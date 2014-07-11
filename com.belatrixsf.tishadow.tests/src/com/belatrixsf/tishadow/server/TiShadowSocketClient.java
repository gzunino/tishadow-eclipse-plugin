package com.belatrixsf.tishadow.server;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import com.belatrixsf.tishadow.preferences.page.PreferenceValues;

public class TiShadowSocketClient implements Callable<Boolean> {
	
	private final class Future extends FutureTask<Boolean> {
		private Future(Callable<Boolean> callable) {
			super(callable);
		}

		@Override
		public void set(Boolean v) {
			super.set(v);
		}
	}
	
	public boolean isADeviceConnected(){
		Future future = new Future(this);
		SocketIO socket = createSocket(future);
		try {
			return future.get(1, TimeUnit.SECONDS);
		} catch (Exception e) {
			if(socket != null){
				socket.disconnect();
			}
			return false;
		}
	}
	
	public SocketIO createSocket(final Future future){
		final JSONObject j = new JSONObject();
		try {
			j.put("name", "controller");
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		
		try {
			final SocketIO socket = new SocketIO("http://" + PreferenceValues.getTishadowHost() + ":" +PreferenceValues.getTishadowPort() + "/");
			socket.connect(new IOCallback() {
				@Override
				public void on(String event, IOAcknowledge ack, Object... args) {
					if (event.equals("device_connect")) {
						future.set(Boolean.TRUE);
						socket.disconnect();
					}
				}

				@Override
				public void onConnect() {
					socket.emit("join", j);
				}

				@Override
				public void onDisconnect() {
				}

				@Override
				public void onError(SocketIOException arg0) {
				}

				@Override
				public void onMessage(String arg0, IOAcknowledge arg1) {
				}

				@Override
				public void onMessage(JSONObject arg0,
						IOAcknowledge arg1) {
				}
	        });
			return socket;
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}

}
