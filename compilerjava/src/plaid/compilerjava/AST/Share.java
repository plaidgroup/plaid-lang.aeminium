package plaid.compilerjava.AST;

import java.util.List;
import java.util.Set;

import plaid.compilerjava.coreparser.Token;
import plaid.compilerjava.tools.ASTVisitor;
import plaid.compilerjava.util.CodeGen;
import plaid.compilerjava.util.IDList;

public class Share implements Expression {
	private Token token;
	private ID group;
	private List<Expression> stmtLists;
	
	public Share(Token token, ID group, List<Expression> stmtLists) {
		this.token = token;
		this.group = group;
		this.stmtLists = stmtLists;
	}

	@Override
	public void codegenExpr(CodeGen out, ID y, IDList localVars, Set<ID> stateVars) {
		// TODO Auto-generated method stub
	}

	@Override
	public Token getToken() {
		return token;
	}

	@Override
	public <T> T accept(ASTVisitor<T> visitor) {
		return visitor.visitNode(this);
	}

	@Override
	public <T> void visitChildren(ASTVisitor<T> visitor) {
		group.accept(visitor);
		for (Expression e : stmtLists)
			e.accept(visitor);
	}
}
