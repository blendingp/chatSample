<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<input type="text" name="chat" id="chat" /> <!--text 입력창 -->
<button onClick="sendMessage()">test</button> <!--send 버튼-->

<div id="chatlist"></div>

<script src="//code.jquery.com/jquery-1.11.0.min.js"></script> 
   <script type="text/javascript">
         var wsUri = "ws://localhost:8080/chatSample/websocket/echo.do"; //주소 확인!!
		 var useridx="";
         function init() {            

            websocket = new WebSocket(wsUri);
            websocket.onopen = function(evt) {
                onOpen(evt);

            };
 
            websocket.onmessage = function(evt) {
                onMessage(evt);
            };
 
            websocket.onerror = function(evt) {
                onError(evt);
            };
 
        }
        
    
        function onOpen(evt) {            
            console.log("onOpen!");
           
        }

    
        function onError(evt) {
            writeToScreen('ERROR: ' + evt.data)
        }
 
        //서버로 패킷 전송 함수 
        function doSend(message) {
            websocket.send(message);
        }                 
        
		function sendMessage(){ //입력받은 메세지를 서버에 전송
    		var chat = $("#chat").val();//입력받은 text를 chat이라는 id를 사용해서 값을 가져온다
    		console.log("chat:"+chat);    		
    		 var obj = new Object();
             obj.protocol = "chat";
             obj.chatdata = chat;
             doSend(JSON.stringify(obj));
    	}
		
        function onMessage(evt) { //받은 메세지를 보여준다
            console.log("onMessage");
        
            var obj = JSON.parse(evt.data);
            if(obj.protocol == "chatresult"){                
            	chatdata = obj.resultdata;
                console.log("chatdata:"+chatdata);
                
                $("#chatlist").append("<div>"+ chatdata+"</div>");
            }
        }
        
        init();
    </script>
