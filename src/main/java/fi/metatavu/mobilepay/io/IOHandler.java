package fi.metatavu.mobilepay.io;

import java.io.IOException;

public interface IOHandler {

	public IOHandlerResult doPost(String url, String data, String authorization) throws IOException;
	
}
