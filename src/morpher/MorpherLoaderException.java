package morpher;

public class MorpherLoaderException extends RuntimeException {

	private MorpherLoaderException(){};
	
	public MorpherLoaderException(String s){
		this(s, new MorpherLoaderException());
	}
	
	public MorpherLoaderException(String msg, Throwable ex){
		super(msg, ex);
		
	}
}
