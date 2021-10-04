
function subscribeHomeMonitor(topic, onMessageHandler){
    client = new Paho.MQTT.Client("localhost", 61614, "BrowserClientId");

    client.onConnectionLost = onConnectionLost;
    client.onMessageArrived = onMessageHandler;

    client.connect(
        {onSuccess:onConnect, onFailure: onConnectionFailed}
    );

    function onConnect() {
      // Once a connection has been made, make a subscription and send a message.
      console.log("onConnect, subscribe to " + topic);
      client.subscribe(topic);
    };

    function onConnectionFailed(responseObject) {
      if (responseObject.errorCode !== 0)
	    console.log("onConnectionFailed:"+responseObject.errorMessage);
    };

    function onConnectionLost(responseObject) {
      if (responseObject.errorCode !== 0)
	    console.log("onConnectionLost:"+responseObject.errorMessage);
    };

}
