package earth.cube.tools.logkeeper.core.test.zmq;

import com.fasterxml.jackson.annotation.JsonProperty;

import earth.cube.tools.logkeeper.core.utils.jackson.JsonEmitter;
import earth.cube.tools.logkeeper.core.utils.jackson.JsonParser;

public class RequestedServerAction {
	
	@JsonProperty("server_action")
	private ServerAction _action;
	
	public RequestedServerAction() {
	}
	
	private RequestedServerAction(ServerAction action) {
		_action = action;
	}
	
	public ServerAction get() {
		return _action;
	}
	
	public static ServerAction probe(String sCnt) {
		ServerAction action;
		if(sCnt != null && sCnt.matches("\\{\\s*\"?server_action\"?.*"))
			action = JsonParser.fromJson(sCnt, RequestedServerAction.class).get();
		else
			action = ServerAction.NONE;
		return action;
	}
	
	public static String create(ServerAction action) {
		RequestedServerAction reqAction = new RequestedServerAction(action);
		return JsonEmitter.toJson(reqAction);
	}

}
