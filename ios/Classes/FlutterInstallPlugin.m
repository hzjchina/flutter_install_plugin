#import "FlutterInstallPlugin.h"

@implementation FlutterInstallPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_install_plugin"
            binaryMessenger:[registrar messenger]];
  FlutterInstallPlugin* instance = [[FlutterInstallPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if([@"gotoAppStore" isEqualToString:call.method]){
      @try {
        NSDictionary *urlStringD = [call arguments];
        //NSLog(@"FlutterInstallPlugin urlString1: %@",[urlStringD description]); 
        NSString *urlString = [urlStringD objectForKey:@"urlString"]; 
        //NSLog(@"FlutterInstallPlugin urlString11: %@",urlString);
        if(urlString == nil) {
            result([FlutterError errorWithCode:@"urlString" message:@"urlString is null" details:NULL]);
        }else{
            //NSLog(@"FlutterInstallPlugin urlString2: %@",urlString);
            //NSURL *url= [NSURL URLWithString:urlString];
            //[[UIApplication sharedApplication]openURL:[NSURLURLWithString:url]];
            [self openScheme:urlString];
        }
       } @catch(FlutterError *e) {
          result(e);
       }
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)openScheme:(NSString *)scheme {
  UIApplication *application = [UIApplication sharedApplication];
  NSURL *URL = [NSURL URLWithString:scheme];
 
 if(@available(iOS 10, *)){
     /*if ([application respondsToSelector:@selector(openURL:options:completionHandler:)]) {
        [application openURL:URL options:@{}
           completionHandler:^(BOOL success) {
          NSLog(@"Open1 %@: %d",scheme,success);
        }];
      } else {
        BOOL success = [application openURL:URL];
        NSLog(@"Open2 %@: %d",scheme,success);
      }*/
     [application openURL:URL options:@{}
        completionHandler:^(BOOL success) {
       //NSLog(@"Open1 %@: %d",scheme,success);
     }];
 }else{
  [application openURL:URL];
  //NSLog(@"FlutterInstallPlugin urlString4: %@",scheme);
 }
}

@end