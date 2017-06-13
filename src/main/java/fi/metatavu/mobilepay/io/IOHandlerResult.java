package fi.metatavu.mobilepay.io;

public class IOHandlerResult {
  
  private String response;
  private int code;
  private String message;
	
	public IOHandlerResult(int code, String message, String response) {
		this.response = response;
		this.message = message;
		this.code = code;
	}

	public int getCode() {
		return code;
	}
	
	public String getResponse() {
		return response;
	}

  public String getMessage() {
    return message;
  }
}