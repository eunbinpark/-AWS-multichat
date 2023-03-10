package views;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;

import dto.request.RequestDto;
import lombok.Getter;
import lombok.Setter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Getter
public class ClientApplication extends JFrame {

	private static final long serialVersionUID = 1L;
	private static ClientApplication instance;

	private Gson gson;
	private Socket socket;

	private JPanel mainPanel;
	private CardLayout mainCard;

	private JTextField usernameField;

	private JTextField sendMessageField;

	@Setter
	private List<Map<String, String>> roomInfoList;
	private DefaultListModel<String> roomNameListModel;
	private DefaultListModel<String> userNameListModel;
	private JList roomList;
	private JList joinUserList;
	
	private JTextArea chattingContent;

	public static ClientApplication getInstance() {
		if (instance == null) {
			instance = new ClientApplication();
		}
		return instance;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientApplication frame = ClientApplication.getInstance();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private ClientApplication() {
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				RequestDto<String> requestDto = new RequestDto<String>("exitRoom", null);
				sendRequest(requestDto);
			}
		});

		/* ========<< init >>======== */

		gson = new Gson();
		try {
			socket = new Socket("192.168.2.101", 9090);
			ClientReceive clientReceive = new ClientReceive(socket);
			clientReceive.start();

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (ConnectException e1) {
			JOptionPane.showMessageDialog(this, "????????? ????????? ??? ????????????.", "????????????", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		} 

		/* ========<< frame set >>======== */

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 150, 480, 800);

		/* ========<< panel >>======== */

		mainPanel = new JPanel();
		JPanel loginPanel = new JPanel();
		JPanel roomListPanel = new JPanel();
		JPanel roomPanel = new JPanel();

		/* ========<< layout >>======== */

		mainCard = new CardLayout(0, 0);

		mainPanel.setLayout(mainCard);
		loginPanel.setLayout(null);
		roomListPanel.setLayout(null);
		roomPanel.setLayout(null);

		/* ========<< panel set >>======== */

		setContentPane(mainPanel);
		mainPanel.add(loginPanel, "loginPanel");
		mainPanel.add(roomListPanel, "roomListPanel");
		mainPanel.add(roomPanel, "roomPanel");

		/* ========<< login panel >>======== */

		JButton enterButton = new JButton("????????????");

		usernameField = new JTextField();
		enterButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					RequestDto<String> usernameCheckReqDto = new RequestDto<String>("usernameCheck",
							usernameField.getText());
					sendRequest(usernameCheckReqDto);
				}
			}
		});

		usernameField.setBounds(59, 449, 333, 45);
		loginPanel.add(usernameField);
		usernameField.setColumns(10);

		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> usernameCheckReqDto = new RequestDto<String>("usernameCheck",
						usernameField.getText());
				sendRequest(usernameCheckReqDto);
			}
		});
		enterButton.setBounds(57, 510, 337, 49);
		loginPanel.add(enterButton);

		/* ========<< roomList panel >>======== */

		JScrollPane rooListScroll = new JScrollPane();
		rooListScroll.setBounds(109, 0, 345, 751);
		roomListPanel.add(rooListScroll);

		roomNameListModel = new DefaultListModel<String>();
		roomList = new JList(roomNameListModel);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					int selectedIndex = roomList.getSelectedIndex();
					RequestDto<Map<String, String>> requestDto = 
							new RequestDto<Map<String,String>>("enterRoom", roomInfoList.get(selectedIndex));
					sendRequest(requestDto);
				}
			}
		});
		rooListScroll.setViewportView(roomList);

		JButton createRoomButton = new JButton("?????????");
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String roomName = null;
				while (true) {
					roomName = JOptionPane.showInputDialog(null, "????????? ?????? ????????? ???????????????.", "?????????",
							JOptionPane.PLAIN_MESSAGE);
					if (roomName == null) {
						return;
					}
					if (!roomName.isBlank()) {
						break;
					}
					JOptionPane.showMessageDialog(null, "????????? ????????? ??? ????????????.", "????????? ??????", JOptionPane.ERROR_MESSAGE);
				}
				RequestDto<String> requestDto = new RequestDto<String>("createRoom", roomName);
				sendRequest(requestDto);
			}
		});
		createRoomButton.setBounds(8, 10, 89, 82);
		roomListPanel.add(createRoomButton);

		/* ========<< room panel >>======== */

		JScrollPane joinUserListScroll = new JScrollPane();
		joinUserListScroll.setBounds(0, 0, 367, 78);
		roomPanel.add(joinUserListScroll);

		userNameListModel = new DefaultListModel<String>();
		joinUserList = new JList(userNameListModel);
		joinUserListScroll.setViewportView(joinUserList);

		JButton roomExitButton = new JButton("?????????");
		roomExitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(JOptionPane.showConfirmDialog(null, "????????? ?????? ??????????????????????", "??? ?????????", JOptionPane.YES_NO_OPTION) == 0) {
					RequestDto<String> requestDto = new RequestDto<String>("exitRoom", null);
					sendRequest(requestDto);
				}
			}
		});
		roomExitButton.setBounds(365, 0, 89, 78);
		roomPanel.add(roomExitButton);

		JScrollPane chattingContentScroll = new JScrollPane();
		chattingContentScroll.setBounds(0, 76, 454, 623);
		roomPanel.add(chattingContentScroll);

		chattingContent = new JTextArea();
		chattingContentScroll.setViewportView(chattingContent);
		chattingContent.setEditable(false);

		sendMessageField = new JTextField();
		
		sendMessageField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					RequestDto<String> requestDto = new RequestDto<String>("sendMessage", sendMessageField.getText());
					sendMessageField.setText("");
					sendRequest(requestDto);
				}
			}
		});
		sendMessageField.setBounds(0, 698, 387, 53);
		roomPanel.add(sendMessageField);
		sendMessageField.setColumns(10);

		JButton sendButton = new JButton("??????");
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> requestDto = new RequestDto<String>("sendMessage", sendMessageField.getText());
				sendMessageField.setText("");
				sendRequest(requestDto);
			}
		});
		sendButton.setBounds(386, 698, 68, 53);
		roomPanel.add(sendButton);
	}

	private void sendRequest(RequestDto<?> requestDto) {
		String reqJson = gson.toJson(requestDto);
		OutputStream outputStream = null;
		PrintWriter printWriter = null;
		try {
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(outputStream, true);
			printWriter.println(reqJson);
			System.out.println("??????????????? -> ?????? : " + reqJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
