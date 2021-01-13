//
//  BridgefySDK.h
//  BridgefySDK
//
//  Created by Bridgefy MackBook Pro on 12/5/19.
//

#import <Cordova/CDVPlugin.h>
#import <BFTransmitter/BFTransmitter.h>

@interface BridgefySDK : CDVPlugin

- (void)initialize:(CDVInvokedUrlCommand*)command;
- (void)deviceListener:(CDVInvokedUrlCommand*)command;
- (void)messageListener:(CDVInvokedUrlCommand*)command;
- (void)start:(CDVInvokedUrlCommand*)command;
- (void)stop:(CDVInvokedUrlCommand*)command;
- (void)sendMessage:(CDVInvokedUrlCommand*)command;
- (void)sendBroadcastMessage:(CDVInvokedUrlCommand*)command;
- (void)connectDevice:(CDVInvokedUrlCommand*)command;
- (void)disconnectDevice:(CDVInvokedUrlCommand*)command;
@end
