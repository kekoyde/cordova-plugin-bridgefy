//
//  BridgefySDK.m
//  BridgefySDK
//
//  Created by Bridgefy MackBook Pro on 12/5/19.
//

#import <Foundation/Foundation.h>
#import "BridgefySDK.h"
#import <BFTransmitter/BFTransmitter.h>

@interface BridgefySDK()<BFTransmitterDelegate>
{}

@property (nonatomic, retain) BFTransmitter * transmitter;
@property (nonatomic, retain) NSMutableDictionary * transitMessages;

@property (nonatomic, retain) id startCallback;
@property (nonatomic, retain) id deviceCallback;
@property (nonatomic, retain) id messageCallback;

@end

@interface BridgefySDK (PrivateMethods)
- (void)sendPluginResult: (id) _callbackId;
@end

@implementation BridgefySDK

#pragma mark - Cordova Commands

- (void)initialize:(CDVInvokedUrlCommand *)command {

    if (self.transmitter != nil)
    {
        [self createClientDictionary];
        [self sendPluginResult:command.callbackId];
    }


    self.transmitter = [[BFTransmitter alloc] initWithApiKey:[command argumentAtIndex:0 withDefault:@"NO_APY_KEY"]];

       if (self.transmitter != nil) {
           self.transmitter.delegate = self;
           NSDictionary * client =  [self createClientDictionary];
           CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: client];
           [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
           _transitMessages = [[NSMutableDictionary alloc] init];
       } else {
           self.transmitter.delegate = self;
           CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Bridgefy could not be initialized."];
           [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
       }

}

- (void)start:(CDVInvokedUrlCommand *)command {
    self.startCallback = command.callbackId;
    if ( self.transmitter == nil ) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Bridgefy was not initialized, the operation won't continue."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }
    NSDictionary * options = [command argumentAtIndex:0 withDefault: nil];
    BOOL autoConnect = [[options valueForKey:@"autoConnect"] boolValue];
    if (autoConnect)
    {
        NSLog(@"BFTransmitterConnectionModeAutomatic");
        [self.transmitter startWithConnectionMode:BFTransmitterConnectionModeAutomatic];
    }
    else
    {
        NSLog(@"BFTransmitterConnectionModeOnDemand");
        [self.transmitter startWithConnectionMode:BFTransmitterConnectionModeOnDemand];
    }

}

- (void)stop:(CDVInvokedUrlCommand *)command {
    [self.transmitter stop];
    [self sendPluginResult:command.callbackId];
}

- (void)messageListener:(CDVInvokedUrlCommand *)command {
    self.messageCallback = command.callbackId;
    NSLog(@"Request messageListener");
}

- (void)deviceListener:(CDVInvokedUrlCommand *)command {
    self.deviceCallback = command.callbackId;
    NSLog(@"Request deviceListener");
}

- (void)sendMessage:(CDVInvokedUrlCommand *)command {
    BFSendingOption options = (BFSendingOptionEncrypted | BFSendingOptionFullTransmission);
    NSDictionary * device = [command argumentAtIndex:0 withDefault:nil];
    NSDictionary * message = [command argumentAtIndex:1 withDefault:nil];
    NSDictionary * bridgefyMessage = @{
        @"content": message,
        @"receiver_id": device[@"userId"]
    };

    [self sendMessage:bridgefyMessage WithOptions:options];
}

- (void)sendBroadcastMessage:(CDVInvokedUrlCommand *)command {
    BFSendingOption options = (BFSendingOptionBroadcastReceiver | BFSendingOptionMeshTransmission);
    NSDictionary * message = [command argumentAtIndex:0 withDefault:nil];
    NSDictionary * bridgefyMessage = @{ @"content": message };
    [self sendMessage:bridgefyMessage WithOptions:options];
}

- (void)connectDevice:(CDVInvokedUrlCommand *)command {
    NSLog(@"Request connectDevice");
    NSString * userId = [command argumentAtIndex:0 withDefault:nil];
    [self connectToUserId:userId callback:command.callbackId];
}

- (void)disconnectDevice:(CDVInvokedUrlCommand *)command {
    NSLog(@"Request disconnectDevice");
    NSString * userId = [command argumentAtIndex:0 withDefault:nil];
    [self disconnectFromUserId:userId callback: command.callbackId];
}

#pragma mark - Handle Cordova wrapper

- (void)sendPluginResult:(id)callbackId {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:SWIFT_CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)sendMessage:(NSDictionary *)message WithOptions: (BFSendingOption)options {

    if (![self transmitterCanWork]) {
        return;
    }

    if (message[@"content"] == nil) {
        NSLog(@"The field 'content' is missing, the message won't be sent: %@", [message description]);
        return;
    }

    if (message[@"receiver_id"] == nil && (options & BFSendingOptionBroadcastReceiver) == 0) {
        NSLog(@"The field 'receiver_id' is missing, the message won't be sent: %@", [message description]);
        return;
    }

    NSError * error = nil;

    NSString * packetID = [self.transmitter sendDictionary:message[@"content"]
                                                    toUser:message[@"receiver_id"]
                                                   options:options
                                                     error:&error];

    NSDictionary * createdMessage = [self createMessageDictionaryWithPayload:message[@"content"]
                                                                      sender:self.transmitter.currentUser
                                                                    receiver:message[@"receiver_id"]
                                                                        uuid:packetID];

    if (error == nil) {
        // Message began the sending process
        self.transitMessages[packetID] = createdMessage;
    } else {
        // Error sending the message
        NSDictionary * errorDict = @{
                                     @"code": @(error.code),
                                     @"description": error.localizedDescription,
                                     @"origin": createdMessage
                                     };

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorDict];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:result callbackId:self.messageCallback];
    }

}

-(void) connectToUserId: (NSString *)userId callback:(id) callbackId {
    CDVPluginResult* result;
    NSDictionary * resposeDict;
    BOOL isNearby = [self.transmitter isUserNearby: userId];
    if (isNearby) {
        NSError* err;
        [self.transmitter connectToUser:userId error:&err];
        if (err) {
            // Error connect to user
             resposeDict = @{ @"code": @(err.code),
                              @"message": err.localizedDescription,
                              @"userId": userId
                            };
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resposeDict];
        } else {
            // Queue request connection
            resposeDict = @{ @"message": [NSString stringWithFormat:@"Trying to connect device userId: %@", userId],
                             @"userId": userId
                           };
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resposeDict];
        }

    } else {
        // User isn't unavailable
        resposeDict = @{ @"message": @"Device not found, try again when DeviceListener reported device detected.",
                         @"userId":  userId
                       };
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resposeDict];
    }

    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

-(void) disconnectFromUserId: (NSString *)userId callback:(id) callbackId {
    CDVPluginResult* result;
    NSDictionary * resposeDict;
    BOOL isNearby = [self.transmitter isUserAvailable: userId];
    if (isNearby) {
        NSError* err;
        [self.transmitter disconnectFromUser:userId error:&err];
        if (err) {
            // Error connect to user
             resposeDict = @{ @"code": @(err.code),
                              @"message": err.localizedDescription,
                              @"userId": userId
                            };
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resposeDict];
        } else {
            // Queue request connection
            resposeDict = @{ @"message": [NSString stringWithFormat:@"Device disconnected userId: %@", userId],
                             @"userId": userId
                           };
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resposeDict];
        }

    } else {
        // User isn't unavailable
        resposeDict = @{ @"message": @"Device not found, try again when DeviceListener reported device connected.",
                         @"userId":  userId
                       };
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resposeDict];
    }
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

#pragma mark - Utils

-(BOOL)transmitterCanWork {
    if ( self.transmitter == nil ) {
        NSLog(@"Bridgefy was not initialized, the operation won't continue.");
        return NO;
    }

    if (!self.transmitter.isStarted) {
        NSLog(@"Bridgefy was not started, the operation won't continue.");
        return NO;
    }

    return YES;
}

- (NSDictionary *)createClientDictionary {
    NSLog(@"Public %@", self.transmitter.localPublicKey);
    NSLog(@"userUUID %@", self.transmitter.currentUser);
    return @{
             @"api_key": @"",
             @"bundle_id": @"",
             @"public_key": [NSString stringWithFormat:@"%@", self.transmitter.localPublicKey],
             @"secret_key": @"",
             @"userUuid": self.transmitter.currentUser,
             @"deviceProfile": @""
             };
}

- (NSDictionary *)createMessageDictionaryWithPayload:(NSDictionary *)payload
                                              sender:(NSString *)sender
                                            receiver:(NSString *) receiver
                                                uuid:(NSString *)uuid {
    NSString * msgReceiver = receiver != nil? receiver : @"";
    NSString * msgUUID = uuid != nil? uuid : @"";

    return @{
             @"receiverId": msgReceiver,
             @"senderId": sender,
             @"uuid": msgUUID,
             @"dateSent": [NSNumber numberWithDouble:[[NSDate dateWithTimeIntervalSince1970:0] timeIntervalSince1970]],
             @"content": payload
             };

}

#pragma mark - BFTransmitterDelegate
- (void)transmitter:(BFTransmitter *)transmitter meshDidAddPacket:(NSString *)packetID {
    if (self.transitMessages[packetID] != nil) {
        [self.transitMessages removeObjectForKey:packetID];
    }
}

- (void)transmitter:(BFTransmitter *)transmitter didReachDestinationForPacket:( NSString *)packetID {

}

- (void)transmitter:(BFTransmitter *)transmitter meshDidStartProcessForPacket:( NSString *)packetID {
    if (self.transitMessages[packetID] != nil) {
        [self.transitMessages removeObjectForKey:packetID];
    }
}

- (void)transmitter:(BFTransmitter *)transmitter didSendDirectPacket:(NSString *)packetID {
    NSDictionary * message = self.transitMessages[packetID];
    if (message == nil) {
        return;
    }
    [self.transitMessages removeObjectForKey:packetID];

    NSDictionary * messageSent = @{
        @"action": @"message_sent",
        @"uuid": packetID
    };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:messageSent];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.messageCallback];
}

- (void)transmitter:(BFTransmitter *)transmitter didFailForPacket:(NSString *)packetID error:(NSError * _Nullable)error {
    NSDictionary * message = self.transitMessages[packetID];
    if (message == nil) {
        return;
    }
    NSDictionary * errorDict = @{
                                 @"code": @(error.code),
                                 @"description": error.localizedDescription,
                                 @"origin": message
                                 };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorDict];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.messageCallback];
    [self.transitMessages removeObjectForKey:packetID];
}

- (void)transmitter:(BFTransmitter *)transmitter meshDidDiscardPackets:(NSArray<NSString *> *)packetIDs {
    //TODO: Implement

}

- (void)transmitter:(BFTransmitter *)transmitter meshDidRejectPacketBySize:(NSString *)packetID {
    //TODO: Implement

}

- (void)transmitter:(BFTransmitter *)transmitter
didReceiveDictionary:(NSDictionary<NSString *, id> * _Nullable) dictionary
           withData:(NSData * _Nullable)data
           fromUser:(NSString *)user
           packetID:(NSString *)packetID
          broadcast:(BOOL)broadcast
               mesh:(BOOL)mesh {
    NSDictionary * message;
    if (broadcast) {
        message = [self createMessageDictionaryWithPayload:dictionary
                                                    sender:user
                                                  receiver:nil
                                                      uuid:packetID];
        NSDictionary * messageBroadcast = @{
            @"action": @"incoming_message_broadcast",
            @"message": message
        };
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:messageBroadcast];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:result callbackId:self.messageCallback];
    } else {
        message = [self createMessageDictionaryWithPayload:dictionary
                                                    sender:user
                                                  receiver: transmitter.currentUser
                                                      uuid:packetID];

        NSDictionary * messageDirect = @{
            @"action": @"incoming_message",
            @"message": message
        };

        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:messageDirect];
        [result setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:result callbackId:self.messageCallback];
    }
}

- (void)transmitter:(BFTransmitter *)transmitter didDetectConnectionWithUser:(NSString *)user {

}

- (void)transmitter:(BFTransmitter *)transmitter didDetectDisconnectionWithUser:(NSString *)user {
    NSDictionary * userDict = @{
                                @"action": @"disconnected",
                                @"device": @{ @"userId": user }
                                };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userDict];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.deviceCallback];
}

- (void)transmitter:(BFTransmitter *)transmitter didFailAtStartWithError:(NSError *)error {

    NSDictionary * errorDict = @{
                                 @"code": @(error.code),
                                 @"message": error.localizedDescription
                                 };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:errorDict];
    [self.commandDelegate sendPluginResult:result callbackId:self.startCallback];
}

- (void)transmitter:(BFTransmitter *)transmitter didOccurEvent:(BFEvent)event description:(NSString *)description {
    if (event == BFEventStartFinished ) {
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Bridgefy Started."];
        [self.commandDelegate sendPluginResult:result callbackId:self.startCallback];
    } else {
        NSDictionary * eventDict = @{
                                     @"code": @(event),
                                     @"description": description
                                     };
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:eventDict];
        [self.commandDelegate sendPluginResult:result callbackId:self.startCallback];
    }
}

- (void)transmitter:(BFTransmitter *)transmitter didDetectSecureConnectionWithUser:(NSString *)user {
    NSDictionary * userDict = @{
                                @"action": @"connected",
                                @"device": [[NSDictionary alloc] initWithObjectsAndKeys: user, @"userId", nil]
                                };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userDict];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.deviceCallback];
}

- (BOOL)transmitter:(BFTransmitter *)transmitter shouldConnectSecurelyWithUser:(NSString *)user {
    return YES;
}

- (void)transmitterNeedsInterfaceActivation:(BFTransmitter *)transmitter {
    //TODO: Implement

}

- (void)transmitterDidDetectAnotherInterfaceStarted:(BFTransmitter *)transmitter {
    //TODO: Implement

}

- (void)transmitter:(nonnull BFTransmitter *)transmitter didDetectNearbyUser:(nonnull NSString *)user {
    NSDictionary * userDict = @{
                                @"action": @"detected",
                                @"device": [[NSDictionary alloc] initWithObjectsAndKeys: user, @"userId", nil]
                                };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userDict];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.deviceCallback];
}


- (void)transmitter:(nonnull BFTransmitter *)transmitter didFailConnectingToUser:(nonnull NSString *)user error:(nonnull NSError *)error {
    NSDictionary * userDict = @{
                                @"action": @"disconnected",
                                @"device": [[NSDictionary alloc] initWithObjectsAndKeys: user, @"userId", nil]
                                };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userDict];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.deviceCallback];
}


- (void)transmitter:(nonnull BFTransmitter *)transmitter userIsNotAvailable:(nonnull NSString *)user {
    NSDictionary * userDict = @{
                                @"action": @"unavailable",
                                @"device": [[NSDictionary alloc] initWithObjectsAndKeys: user, @"userId", nil]
                                };
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userDict];
    [result setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:result callbackId:self.deviceCallback];
}


@end
