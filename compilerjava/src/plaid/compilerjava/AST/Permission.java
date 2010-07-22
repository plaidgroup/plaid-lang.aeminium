package plaid.compilerjava.AST;

import plaid.compilerjava.coreparser.Token;
import plaid.compilerjava.tools.ASTVisitor;

public class Permission implements ASTnode {
	public static final Permission FULL = new Permission(null, "full");
	public static final Permission IMMUTABLE = new Permission(null, "immutable");
	public static final Permission PURE = new Permission(null, "pure");
	public static final Permission DYN = new Permission(null, "dyn");
	public static final Permission UNIQUE = new Permission(null, "unique");
	public static final Permission SHARED = new Permission(null, "shared");
	
	// This is a marker to indicate that the permission of the receiver should remain unchanged
	public static final Permission RECEIVER = new Permission(null, "receiver");
	
	// This is a marker to indicate that the permission of the object should be the default of the state declaration
	public static final Permission DEFAULT = new Permission(null, "default");

	private final Token token;
	private final String image;
	
	public Permission(Token token, String image) {
		this.token = token;
		this.image = image;
	}
	
	public String getImage() {
		return this.image;
	}

	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitNode(this);
	}

	@Override
	public Token getToken() {
		return this.token;
	}

	@Override
	public <T> void visitChildren(ASTVisitor<T> visitor) {
		// no children
	}

	public String toString() {
		return "Permission: " + this.image;
	}
}
