package views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

import dto.response.ResponseDto;

public class ClientReceive extends Thread {

	private Socket socket;
	private Gson gson;
	
	public ClientReceive(Socket socket) {
		this.socket = socket;
		gson = new Gson();
	}
	
	@Override
	public void run() {
		
		try {
			InputStream inputStream = socket.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while(true) {
				String responseJson = bufferedReader.readLine();
				responseMapping(responseJson);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void responseMapping(String responseJson) {
		ResponseDto<?> responseDto = gson.fromJson(responseJson, ResponseDto.class);
		switch (responseDto.getResource()) {
		case "usernameCheckIsBlank":
//			JOptionPane.showMessageDialog(null, (String)responseDto.getBody(), "접속오류", JOptionPane.WARNING_MESSAGE);
//			break;
			// usernameCheckIsBlank 또는 usernameCheckIsDuplicate 넘어오면 아래 실행(or처럼 처리)
		case "usernameCheckIsDuplicate":
			JOptionPane.showMessageDialog(null, (String)responseDto.getBody(), "접속오류", JOptionPane.WARNING_MESSAGE);
			break;
			
		case "usernameCheckSuccessfully":
			
			break;
			
		default:
			break;
		}
	}
}
