
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.zip.CRC32;
import java.nio.ByteBuffer;
public class Ex2Client{

	//function that returns the CRC32 code of an array of byte in 4 byte array
	public static byte[] getCRC32Code(byte[] message){
		CRC32 crc = new CRC32();
		//get the CRC32 code of the message
		crc.update(message);
		//Converted the code into 8 byte array
		ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
		byteBuffer.putLong(crc.getValue());
		//Converted the eight byte array to four byte array
		byte[] crcEightByte = byteBuffer.array();
		byte[] crcFourByte = new byte[4];
		for(int i = 4; i<8; i++){
			crcFourByte[i-4] = crcEightByte[i];
		}
		return crcFourByte;
	}

	//funtion that prints the byte array into hex format string. Maximum byte
	//per line is 10 with a 2 space indentation at the beginning of each line.
	public static void printBytesInHex(byte[] bytes){
		for(int i = 0; i<bytes.length; i++){
			if(i%10 == 0 && i != 0){
				System.out.println();
				System.out.print("  ");
			}
			if(i == 0){
				System.out.print("  ");
			}
			System.out.print(String.format("%02X", bytes[i]));
		}
		System.out.println();
	}

	//Main program
	public static void main(String[] args)throws Exception{
		//connect to server
		try(Socket socket = new Socket("codebank.xyz", 38102)){
			InputStream fromServer = socket.getInputStream();
			OutputStream toServer = socket.getOutputStream();
			byte[] firstByte;
			byte[] secondByte ;
			byte[]	messageReceived = new byte[100];
			int counter = 0;

			//while haven't finish reading 100 byte message yet
			while(counter < messageReceived.length){
				//allocate two array to store two different half byte
				firstByte = new byte[1];
				secondByte = new byte[1];
				//read two bytes from server each time
				fromServer.read(firstByte);
				fromServer.read(secondByte);
				//shift first byte 4 to left and add it with second half byte to get one byte message.
				int a = firstByte[0] << 4;
				int b = secondByte[0];
				int c = a + b;
				//store this byte to message received array
				messageReceived[counter] = (byte)c;
				counter++;
			}

			System.out.println("Connected to server.");
			System.out.println("Received bytes:");
			//print all byte recieved
			printBytesInHex(messageReceived);
			//get the CRC32 Code in 4 byte array
			byte[] crc32Code = getCRC32Code(messageReceived);

			System.out.print("Generated CRC32:");
			//print CRC32 code in hex
			printBytesInHex(crc32Code);
			//send CRC32 code to server
			toServer.write(crc32Code);
			//get response from server
			firstByte = new byte[1];
			fromServer.read(firstByte);
			//response message according to response from server
			if(firstByte[0] == 1){
				System.out.println("Response good.");
			}
			else{
				System.out.println("Response bad.");
			}
		}
		catch(Exception e){
			System.out.println("error");
		}
		System.out.println("Disconnected from server.");
	}
}

