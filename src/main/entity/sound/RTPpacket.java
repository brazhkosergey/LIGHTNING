package entity.sound;

public class RTPpacket {
	// size of the RTP header:
	private static int HEADER_SIZE = 12;

	private int Version;
	private int Padding;
	private int Extension;
	private int CC;
	private int Marker;
	private int PayloadType;
	private int SequenceNumber;
	private int TimeStamp;
	private int Ssrc;

	private byte[] header;

	private int payload_size;
	private byte[] payload;

	RTPpacket(byte[] packet, int packet_size) {
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 0;
		if (packet_size >= HEADER_SIZE) {
			header = new byte[HEADER_SIZE];
			for (int i = 0; i < HEADER_SIZE; i++)
				header[i] = packet[i];
			payload_size = packet_size - HEADER_SIZE;
			payload = new byte[payload_size];
			for (int i = HEADER_SIZE; i < packet_size; i++){
				payload[i - HEADER_SIZE] = packet[i];
			}

			// interpret the changing fields of the header:
			PayloadType = header[1] & 127;
			SequenceNumber = unsigned_int(header[3]) + 256
					* unsigned_int(header[2]);
			TimeStamp = unsigned_int(header[7]) + 256 * unsigned_int(header[6])
					+ 65536 * unsigned_int(header[5]) + 16777216
					* unsigned_int(header[4]);
		}
	}

	/**
	 * getpayload: return the payload bistream of the RTPpacket and its size
	 * @param data
	 */
	void getpayload(byte[] data) {
		System.arraycopy(payload, 0, data, 0, payload_size);
	}

	// --------------------------
	// getpayload_length: return the length of the payload
	// --------------------------
	public int getpayload_length() {
		return (payload_size);
	}

	// --------------------------
	// getlength: return the total length of the RTP packet
	// --------------------------
	public int getlength() {
		return (payload_size + HEADER_SIZE);
	}

	// --------------------------
	// getpacket: returns the packet bitstream and its length
	// --------------------------
	public int getpacket(byte[] packet) {
		// construct the packet = header + payload
		for (int i = 0; i < HEADER_SIZE; i++)
			packet[i] = header[i];
		for (int i = 0; i < payload_size; i++)
			packet[i + HEADER_SIZE] = payload[i];

		// return total size of the packet
		return (payload_size + HEADER_SIZE);
	}

	// --------------------------
	// gettimestamp
	// --------------------------

	public int gettimestamp() {
		return (TimeStamp);
	}

	// --------------------------
	// getsequencenumber
	// --------------------------
	public int getsequencenumber() {
		return (SequenceNumber);
	}

	// --------------------------
	// getpayloadtype
	// --------------------------
	public int getpayloadtype() {
		return (PayloadType);
	}

	// --------------------------
	// print headers without the SSRC
	// --------------------------
	public void printheader() {

		for (int i = 0; i < (HEADER_SIZE - 4); i++) {
			for (int j = 7; j >= 0; j--)
				if (((1 << j) & header[i]) != 0)
					System.out.print("1");
				else
					System.out.print("0");
			System.out.print(" ");
		}

		System.out.println();
	}

	// return the unsigned value of 8-bit integer nb
	static int unsigned_int(int nb) {
		if (nb >= 0)
			return (nb);
		else
			return (256 + nb);
	}

}
