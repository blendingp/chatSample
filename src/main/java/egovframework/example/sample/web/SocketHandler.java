	package egovframework.example.sample.web;
	 
	import java.util.HashSet;
	import java.util.Map;
	import java.util.Set;
	
	import javax.annotation.Resource;
	
	import org.apache.log4j.LogManager;
	import org.apache.log4j.Logger;
	import org.json.simple.JSONArray;
	import org.json.simple.JSONObject;
	import org.json.simple.parser.JSONParser;
	import org.springframework.beans.factory.InitializingBean;
	import org.springframework.web.socket.CloseStatus;
	import org.springframework.web.socket.TextMessage;
	import org.springframework.web.socket.WebSocketMessage;
	import org.springframework.web.socket.WebSocketSession;
	import org.springframework.web.socket.handler.TextWebSocketHandler;
	
	import egovframework.example.sample.service.impl.SampleDAO;
	 
	public class SocketHandler extends TextWebSocketHandler implements InitializingBean {
	    @Resource(name = "sampleDAO")
	    private SampleDAO sampleDAO;		        	    
	    private final Logger logger = LogManager.getLogger(getClass());	 
	    private Set<WebSocketSession> sessionSet = new HashSet<WebSocketSession>();	 
	    public SocketHandler() {	 
	        super();	 	
	    }
	 
	    @Override	 
	    public void afterConnectionClosed(WebSocketSession session,	 
	            CloseStatus status) throws Exception {
	        super.afterConnectionClosed(session, status);
	        sessionSet.remove(session);
	        this.logger.info("remove session!");
	    }
	 
	    @Override
	 
	    public void afterConnectionEstablished(WebSocketSession session)
	            throws Exception {
	        super.afterConnectionEstablished(session);
	        sessionSet.add(session);
	        try{
	        	System.out.println("접속 아이디 부여:"+session.getId());
	        	Map<String, Object> m = session.getAttributes();
	            m.put("useridx", session.getId());
	        }catch(Exception ignored){}
	        this.logger.info("add session!");
	    }
	    
	 
	    @Override
	    public void handleMessage(WebSocketSession session,
	            WebSocketMessage<?> message) throws Exception {
	        super.handleMessage(session, message);
	        this.logger.info("receive message:" + message.getPayload() );
	        String msg = ""+message.getPayload();
	        JSONParser p = new JSONParser();
	        JSONObject obj = (JSONObject)p.parse(msg);
	      
	        //입력받은 메세지를 기져와서 이용자에게 보내기
	        if(("" + obj.get("protocol")).compareTo("chat") == 0)
	        {
	        	System.out.println("chat start");
	        	String chatdata = "" + obj.get("chatdata");
	        	System.out.println("chatdata: " + chatdata);
	        	
	        	JSONObject robj = new JSONObject();
	        	robj.put("protocol", "chatresult");
	        	robj.put("resultdata", chatdata);
	            sendMessage(robj.toString());
	        }
	    }
	    
	    @Override
	    public void handleTransportError(WebSocketSession session,
	            Throwable exception) throws Exception {
	        this.logger.error("web socket error!", exception);
	    }
	 
	    @Override
	    public boolean supportsPartialMessages() {
	        this.logger.info("call method!");
	        return super.supportsPartialMessages();
	    }
	 
	    public void sendMessage(String message) { //입력받은 메세지를 접속해있는 모든 사용자에게 보여준다
	        System.out.println("sendMessage:"+message);
	        for (WebSocketSession session : this.sessionSet) {
	            if (session.isOpen()) {
	                try {
	                    session.sendMessage(new TextMessage(message));
	                } catch (Exception ignored) {
	                    this.logger.error("fail to send message!", ignored);
	                }
	            }
	        }
	    }
	 
	    public void sendMessageTo(JSONObject obj) {
	        String sender = ""+obj.get("senderidx");
	        String target = ""+obj.get("recvidx");
	        System.out.println("sendMessageTo sender:"+sender);
	        
	        for (WebSocketSession session : this.sessionSet) {
	            if (session.isOpen()) {
	                Map<String, Object> m = session.getAttributes();
	                String useridx = ""+m.get("useridx");
	                if(useridx.compareTo(target)!=0 && useridx.compareTo(sender)!=0
	                        &&target.compareTo("-1")!=0)
	                {
	                    System.out.println("pass nextTurn");
	                    continue;
	                }
	                try {
	                    session.sendMessage(new TextMessage(obj.toString()));
	                } catch (Exception ignored) {
	                    this.logger.error("fail to send message!", ignored);
	                }
	            }
	        }
	    }
	    
	    @Override
	    public void afterPropertiesSet() throws Exception {
	        Thread thread = new Thread() {
	            int i = 0;
	            
	            @Override
	            public void run() {
	                while (true) {
	                    try {
	                        //sendMessage("send message index " + i++);
	                        Thread.sleep(1000);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                        break;
	                    }
	                }
	            }
	        };
	        thread.start();
	    }
}