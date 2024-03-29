package typechecker.tests.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import plaid.compilerjava.AST.Permission;
import plaid.compilerjava.AST.TypeDecl;
import plaid.compilerjava.util.FieldRep;
import plaid.compilerjava.util.MemberRep;
import plaid.compilerjava.util.MethodRep;
import plaid.compilerjava.util.QualifiedID;
import plaid.compilerjava.util.StateRep;
import plaid.runtime.PlaidConstants;
import plaid.runtime.PlaidException;
import plaid.runtime.PlaidMemberDef;
import plaid.runtime.PlaidMethod;
import plaid.runtime.PlaidObject;
import plaid.runtime.PlaidRuntime;
import plaid.runtime.PlaidState;
import plaid.runtime.Util;
import plaid.runtime.models.map.PlaidJavaObjectMap;
import plaid.runtime.models.map.PlaidStateMap;
import plaid.runtime.utils.Delegate;
import plaid.runtime.annotations.RepresentsState;
import plaid.typechecker.AST.Application;
import plaid.typechecker.AST.CompilationUnit;
import plaid.typechecker.AST.Dereference;
import plaid.typechecker.AST.DynPermType;
import plaid.typechecker.AST.FieldTypeDecl;
import plaid.typechecker.AST.FullPermission;
import plaid.typechecker.AST.ID;
import plaid.typechecker.AST.ImmutablePermission;
import plaid.typechecker.AST.ImportList;
import plaid.typechecker.AST.IntLiteral;
import plaid.typechecker.AST.Lambda;
import plaid.typechecker.AST.MethodDecl;
import plaid.typechecker.AST.MethodTypeDecl;
import plaid.typechecker.AST.NonePermission;
import plaid.typechecker.AST.PermType;
import plaid.typechecker.AST.PurePermission;
import plaid.typechecker.AST.SharedPermission;
import plaid.typechecker.AST.StringLiteral;
import plaid.typechecker.AST.Type;
import plaid.typechecker.AST.UnannotatedLetBinding;
import plaid.typechecker.AST.UniquePermission;
import plaid.typechecker.AST.UnitLiteral;
import plaid.typechecker.AST.visitor.AeminiumCodeGenVisitor;
import plaid.typechecker.AST.visitor.CodeGenVisitor;
import plaid.typechecker.AST.visitor.DependencyVisitor;
import plaid.typechecker.AST.visitor.PrintVisitor;
import plaid.typechecker.AST.visitor.TypecheckerVisitor;
import plaid.typechecker.AST.visitor.helper.Context;

/**
 * TODO: We may want to merge all of these constructors into the actual Plaid 
 * code generation.  This could potentially speed up execution a lot by not 
 * requiring reloading the class every time.
 * 
 * @author mhahnenberg
 *
 */
public class TestUtils {
	public static PlaidObject convertJavaListToPlaidList(List<PlaidObject> javaList) {
		PlaidMethod convertMethod = Util.toPlaidMethod(Util.lookup("plaid.lang.globals.javaListToPlaidList", Util.unit()));
		return convertMethod.invoke(new PlaidJavaObjectMap(javaList));
	}
	
	/**
	 * Constructs a new Lambda AST node.
	 * 
	 * @param x TODO
	 * @param body TODO
	 * @param methodType TODO
	 * @return The newly created Lambda AST node.
	 */
	public static PlaidObject lambda(final PlaidObject x, final PlaidObject body, final PlaidObject methodType) {
		PlaidState newState = Util.newState();
		newState.addMember(Util.anonymousMemberDef("x", false, false), protoField(x));
		newState.addMember(Util.anonymousMemberDef("body", false, false), protoField(body));
		newState.addMember(Util.anonymousMemberDef("methodType", false, false), protoField(methodType));
		
		return initAndInstantiateState(Lambda.Lambda, newState);
	}	
	
	/**
	 * Constructs a new Application node using the given function and argument.
	 * 
	 * @param f TODO
	 * @param arg TODO
	 * @return The newly created Application AST node.
	 */
	public static PlaidObject application(final PlaidObject f, final PlaidObject arg) {
		PlaidState newState = Util.newState();
		newState.addMember(Util.anonymousMemberDef("f", false, false), protoField(f));
		newState.addMember(Util.anonymousMemberDef("arg", false, false), protoField(arg));
		
		return initAndInstantiateState(Application.Application, newState);
	}
	
	/**
	 * Constructs a new CompilationUnit node.
	 * 
	 * @return The newly created CompilationUnit AST node.
	 */
	public static PlaidObject compilationUnit(final List<PlaidObject> decls, final List<String> packageName,
											  final PlaidObject imports) {
		
		PlaidState newState = Util.newState();
		newState.addMember(Util.anonymousMemberDef("decls", false, false),
			protoField(convertJavaListToPlaidList(decls)));
		newState.addMember(Util.anonymousMemberDef("packageName", false, false),
				protoField(new PlaidJavaObjectMap(packageName)));
		newState.addMember(Util.anonymousMemberDef("imports", false, false),
				protoField(imports));		
		
		return initAndInstantiateState(CompilationUnit.CompilationUnit, newState);
	}

	/**
	 * Constructs a new ImportList node.
	 * 
	 * @return The newly created ImportList AST node.
	 */
	public static PlaidObject importList(final List<QualifiedID> qids) {
		
		PlaidState newState = Util.newState();
		newState.addMember(Util.anonymousMemberDef("imports", false, false),
				protoField(new PlaidJavaObjectMap(qids)));		
		
		return initAndInstantiateState(ImportList.ImportList, newState);
	}	
	
	/**
	 * Constructs a new UnannotatedLetBinding using the given variable, 
	 * expression, and body.
	 * 
	 * @param x The ID of the variable in the let expression.
	 * @param exp The expression to bind to the variable in the body of the let 
	 * 	expression.
	 * @param body The body of the let expression.
	 * @return The newly created UnannotatedLetBinding AST node.
	 */
	public static PlaidObject let(final PlaidObject x, final PlaidObject exp, final PlaidObject body) {	
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("x", false, false), 
				protoField(x));
		newState.addMember(Util.anonymousMemberDef("exp", false, false), 
				protoField(exp));
		newState.addMember(Util.anonymousMemberDef("body", false, false), 
				protoField(body));
		newState.addMember(Util.anonymousMemberDef("mutable", false, false), 
				protoField(Util.falseObject()));
		
		// instantiate the new prototype
		return initAndInstantiateState(UnannotatedLetBinding.UnannotatedLetBinding, 
				newState);
	}
		
	/**
	 * Constructs a new ID using the given name.
	 * 
	 * @param name The name of the ID.
	 * @return The newly created ID AST node.
	 */
	public static PlaidObject id(final String name) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("name", false, false), 
				protoField(Util.string(name)));
		
		// instantiate the new prototype
		return initAndInstantiateState(ID.ID, newState);
	}

	/**
	 * Constructs a new ID using the given name and permtype.
	 * 
	 * @param name The name of the ID.
	 * @param permType The permtype.
	 * @return The newly created ID AST node.
	 */
	public static PlaidObject id(final String name, final PlaidObject permType) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("name", false, false), 
				protoField(Util.string(name)));
		newState.addMember(Util.anonymousMemberDef("permType", false, false),
				protoField(permType));
		
		// instantiate the new prototype
		return initAndInstantiateState(ID.ID, newState);
	}
	
	/**
	 * Constructs a new IntLiteral given an integer value.
	 * 
	 * @param num The value of the IntLiteral AST node.
	 * @return The newly created IntLiteral AST node.
	 */
	public static PlaidObject intLiteral(final int num) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("integer", false, false), 
				protoField(Util.integer(num)));
		
		// instantiate the new prototype
		return initAndInstantiateState(IntLiteral.IntLiteral, newState);
	}
	
	/**
	 * Constructs a new IntLiteral given an integer value and a permtype.
	 * 
	 * @param num The value of the IntLiteral AST node.
	 * @param permType TODO
	 * @return The newly created IntLiteral AST node.
	 */
	public static PlaidObject intLiteral(final int num, final PlaidObject permType) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("integer", false, false), 
				protoField(Util.integer(num)));
		newState.addMember(Util.anonymousMemberDef("permType", false, false),
				protoField(permType));
		
		// instantiate the new prototype
		return initAndInstantiateState(IntLiteral.IntLiteral, newState);
	}

	/**
	 * Constructs a new StringLiteral given a string.
	 * 
	 * @param num The value of the StringLiteral AST node.
	 * @return The newly created StringLiteral AST node.
	 */
	public static PlaidObject stringLiteral(final String str) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("string", false, false), 
				protoField(Util.string(str)));
		
		// instantiate the new prototype
		return initAndInstantiateState(StringLiteral.StringLiteral, newState);
	}
	
	/**
	 * Constructs a new UnitLiteral.
	 * 
	 * @return The newly created UnitLiteral AST node.
	 */
	public static PlaidObject unitLiteral() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(UnitLiteral.UnitLiteral, newState);
	}
	
	/**
	 * Constructs a new Dereference node.
	 * 
	 * @param left TODO
	 * @param right TODO
	 * @return The newly created Dereference AST node.
	 */
	public static PlaidObject dereference(final PlaidObject left, final PlaidObject right) {
		// create a new blank prototype
		PlaidState newState = Util.newState();

		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("left", false, false), 
				protoField(left));
		newState.addMember(Util.anonymousMemberDef("right", false, false), 
				protoField(right));
		
		// instantiate the new prototype
		return initAndInstantiateState(Dereference.Dereference, newState);
	}	
	
	/**
	 * Constructs a new TypecheckerVisitor to use for typechecking tests.
	 * 
	 * @return The newly created TypecheckerVisitor.
	 */
	public static PlaidObject typechecker() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("context", false, false), protoField(context()));
		
		// instantiate the new prototype
		return initAndInstantiateState(TypecheckerVisitor.TypecheckerVisitor, newState);
	}

	/**
	 * Constructs a new PrintVisitor to output a Plaid AST.
	 * 
	 * @return The newly created PrintVisitor.
	 */
	public static PlaidObject printVisitor() {
		PlaidState newState = Util.newState();
		return initAndInstantiateState(PrintVisitor.PrintVisitor, newState);
	}
	
	/**
	 * Constructs a new DependencyVisitor.
	 * 
	 * @return The newly created DependencyVisitor.
	 */
	public static PlaidObject dependencyVisitor() {
		PlaidState newState = Util.newState();
		return initAndInstantiateState(DependencyVisitor.DependencyVisitor, newState);
	}
	
	/**
	 * Constructs a new Plaid CodeGenVisitor.
	 * 
	 * @return The newly created Plaid CodeGenVisitor.
	 */
	public static PlaidObject plaidCodeGenVisitor() {
		PlaidState newState = Util.newState();
		return initAndInstantiateState(CodeGenVisitor.CodeGenVisitor, newState);
	}
	
	/**
	 * Constructs a new Aeminium CodeGenVisitor.
	 * 
	 * @return The newly created Aeminium CodeGenVisitor.
	 */
	public static PlaidObject aeminiumCodeGenVisitor() {
		PlaidState newState = Util.newState();
		return initAndInstantiateState(AeminiumCodeGenVisitor.AeminiumCodeGenVisitor, newState);
	}
	
	/**
	 * Constructs a new Context object to use for typechecking tests.
	 * 
	 * @return The newly created Context object.
	 */
	public static PlaidObject context() {
		PlaidState newState = Util.newState();
		
		return initAndInstantiateState(Context.Context, newState);
	}
	
	/**
	 * Constructs a new PermType based on the given Permission and Type.
	 * 
	 * @param perm
	 * @param type
	 * @return
	 */
	public static PlaidObject permtype(PlaidObject perm, PlaidObject type) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("perm", false, false), 
				protoField(perm));
		newState.addMember(Util.anonymousMemberDef("type", false, false), 
				protoField(type));
		
		// instantiate the new prototype
		return initAndInstantiateState(PermType.PermType, newState);
	}
	
	
	
	/**
	 * Constructs a new Type object based upon the specified type abbreviations 
	 * and declarations.
	 * 
	 * @param abbrevs Array of ID objects for the abbreviations in the PermType.
	 * @param decls Array of TypeDecl objects (MethodTypeDecl or FieldTypeDecl).
	 * @return
	 */
	public static PlaidObject type(PlaidObject[] abbrevs, PlaidObject[] decls) {
		// fill in the sets
		Set<PlaidObject> typeAbbrevs = new HashSet<PlaidObject>();
		for (PlaidObject abbrev : abbrevs) {
			typeAbbrevs.add(abbrev);
		}
		Set<PlaidObject> typeDecls = new HashSet<PlaidObject>();
		for (PlaidObject decl : decls) {
			typeDecls.add(decl);
		}
	
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("typeAbbrevs", false, false), 
				protoField(new PlaidJavaObjectMap(typeAbbrevs)));
		newState.addMember(Util.anonymousMemberDef("typeDecls", false, false), 
				protoField(new PlaidJavaObjectMap(typeDecls)));
		
		// instantiate the new prototype
		return initAndInstantiateState(Type.Type, newState);
	}
	
	/**
	 * TODO
	 * @param name
	 * @param body
	 * @param arg
	 * @param abstractMethod
	 * @üaram methodType
	 * @return
	 */
	public static PlaidObject methodDecl(PlaidObject name,
									 PlaidObject body,
									 PlaidObject arg,
									 PlaidObject abstractMethod,
									 PlaidObject methodType) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("name", false, false), 
				protoField(name));
		newState.addMember(Util.anonymousMemberDef("body", false, false), 
				protoField(body));
		newState.addMember(Util.anonymousMemberDef("arg", false, false), 
				protoField(arg));
		newState.addMember(Util.anonymousMemberDef("abstractMethod", false, false), 
				protoField(abstractMethod));
		newState.addMember(Util.anonymousMemberDef("methodType", false, false), 
				protoField(methodType));

		// instantiate the new prototype
		return initAndInstantiateState(MethodDecl.MethodDecl, newState);	
	}
	
	/**
	 * TODO: add type change information
	 * @param name
	 * @param retPermType
	 * @param argTypes
	 * @param argNames
	 * @return
	 */
	public static PlaidObject methodType(PlaidObject name,
										 PlaidObject retPermType, 
										 PlaidObject argTypes,
										 PlaidObject argNames) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("name", false, false), 
				protoField(name));
		newState.addMember(Util.anonymousMemberDef("retPermType", false, false), 
				protoField(retPermType));
		newState.addMember(Util.anonymousMemberDef("argTypes", false, false), 
				protoField(argTypes));
		newState.addMember(Util.anonymousMemberDef("argNames", false, false), 
				protoField(argNames));
		
		// instantiate the new prototype
		return initAndInstantiateState(MethodTypeDecl.MethodTypeDecl, newState);
	}
	
	/**
	 * Constructs a new FieldTypeDecl AST Node given a PermType for the field.
	 * @param name ID of the field.
	 * @param permType PermType of the field.
	 * @return
	 */
	public static PlaidObject fieldType(PlaidObject name, PlaidObject permType) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("name", false, false), 
				protoField(name));
		newState.addMember(Util.anonymousMemberDef("permType", false, false), 
				protoField(permType));
		
		// instantiate the new prototype
		return initAndInstantiateState(FieldTypeDecl.FieldTypeDecl, newState);
	}
	
	/**
	 * Fetches the specified field from within the given PlaidObject.
	 * 
	 * @param fieldName
	 * @param obj
	 * @return
	 */
	public static PlaidObject getField(String fieldName, PlaidObject obj) {
		Map<PlaidMemberDef, PlaidObject> members = obj.getMembers();
		for (Entry<PlaidMemberDef, PlaidObject> member : members.entrySet()) {
			if (member.getKey().getMemberName().equals(fieldName)) {
				return member.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Takes a PlaidObject representing a structural type (Type) and returns 
	 * the Java Set containing the type abbreviations in that structural type.
	 * 
	 * @param structType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<PlaidObject> getTypeAbbrevs(PlaidObject structType) {
		return (Set<PlaidObject>)Util.toPlaidJavaObject(TestUtils.getField("typeAbbrevs", structType)).getJavaObject();
	}
	
	/**
	 * Takes a PlaidObject representing a structural type (Type) and returns
	 * the Java Set containing the type declarations in that structural type
	 * (methods and fields).
	 * 
	 * @param structType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<PlaidObject> getTypeDecls(PlaidObject structType) {
		return (Set<PlaidObject>)Util.toPlaidJavaObject(TestUtils.getField("typeDecls", structType)).getJavaObject();
	}
	
	/**
	 * Creates a new ProtoField that simply initializes the field with whatever
	 * object is passed to the function.
	 * 
	 * @param obj The object with which the field will be initialized.
	 * @return The new ProtoField.
	 */
	private static PlaidMethod protoField(final PlaidObject obj) {
		return Util.protoField(new Delegate() {
			@Override
			public PlaidObject invoke(PlaidObject thisVar, PlaidObject args) throws PlaidException {
				return obj;
			}
		});
	}
	
	/**
	 * Initializes and instantiates a new state based upon the prototype object
	 * stored in the generated Java class (usually called <state name>.<state name>)
	 * 
	 * @param prototypeObj
	 * @param newState
	 * @return
	 */
	private static PlaidObject initAndInstantiateState(PlaidObject prototypeObj, PlaidState newState) {
		// get the prototype for the state
		PlaidStateMap prototype = new PlaidStateMap();
		prototype.setPrototype(prototypeObj);
		
		// initialize the rest of the new object with the old prototype
		PlaidState initializedState = prototype.initState(newState);
		
		// instantiate the new prototype
		return initializedState.instantiate();
	}
	
	private static PlaidObject stateRepToStructuralType(StateRep rep) {
		PlaidObject[] typeAbbrevs = {
			Util.string(rep.getName())
		};
		
		List<MemberRep> members = rep.getMembers();
		List<PlaidObject> typeDecls = new ArrayList<PlaidObject>();
		
		for (MemberRep member : members) {
			typeDecls.add(TestUtils.memberRepToPlaidType(member));
		}
		
		return TestUtils.type(typeAbbrevs, (PlaidObject[])typeDecls.toArray());
	}

	@SuppressWarnings("unchecked")
	public static PlaidObject getStructuralTypeFromAbbrev(String name) {
		// check if we can find the file 
		ClassLoader cl = PlaidRuntime.getRuntime().getClassLoader().getClass().getClassLoader();
		
		String names[] = {name + PlaidConstants.ID_SUFFIX, name};
		Class<Object> obj = null;
		for (String current : names) {
			try {
				obj = (Class<Object>)cl.loadClass(current);
				break;
			} catch (ClassNotFoundException e) {
				// If there is no classfile then we need to keep searching
			}
		}
		
		if (obj == null) {
			return null;
		}
		
		RepresentsState stateAnnotation = obj.getAnnotation(RepresentsState.class);
		
		// TODO: get the type out of the state annotation and return it
		StateRep stateRep = StateRep.parseJSONObject((JSONObject)JSONValue.parse(stateAnnotation.jsonRep()));
		
		return TestUtils.stateRepToStructuralType(stateRep);
	}

	public static void addToContext(PlaidObject context, PlaidObject x, PlaidObject xPermType) {
		Util.call(TestUtils.getField("put", context), Util.packPlaidObjectsIntoArray(x, xPermType));
	}
	
	/*
	 *************************************************************************
	 *                             New stuff                                 *
	 *************************************************************************
	 */
	
	/**
	 * Constructs a new UniquePermission object.
	 * 
	 * @return The new UniquePermission object;
	 */
	public static PlaidObject unique() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(UniquePermission.UniquePermission, newState);
	}
	
	/**
	 * Constructs a new FullPermission object.
	 * 
	 * @return The new FullPermission object;
	 */
	public static PlaidObject full() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(FullPermission.FullPermission, newState);
	}
	
	/**
	 * Constructs a new SharedPermission object.
	 * 
	 * @return The new SharedPermission object;
	 */
	public static PlaidObject shared() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(SharedPermission.SharedPermission, newState);
	}
	
	/**
	 * Constructs a new ImmutablePermission object.
	 * 
	 * @return The new ImmutablePermission object;
	 */
	public static PlaidObject immutable() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(ImmutablePermission.ImmutablePermission, newState);
	}
	
	/**
	 * Constructs a new PurePermission object.
	 * 
	 * @return The new PurePermission object;
	 */
	public static PlaidObject pure() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(PurePermission.PurePermission, newState);
	}
	
	/**
	 * Constructs a new NonePermission object.
	 * 
	 * @return The new NonePermission object;
	 */
	public static PlaidObject none() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(NonePermission.NonePermission, newState);
	}
	
	// TODO: is this where we should do this?
	private static PlaidObject dyn() {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		// instantiate the new prototype
		return initAndInstantiateState(DynPermType.DynPermType, newState);
	}
	
	private static PlaidObject javaPermTypeToPlaidPermType(plaid.compilerjava.AST.PermType permType) {
		// create a new blank prototype
		PlaidState newState = Util.newState();
		
		PlaidObject perm = TestUtils.javaPermToPlaidPerm(permType.getPermission());
		PlaidObject type = TestUtils.type(permType.getType());
		
		// add the necessary members to the new prototype
		newState.addMember(Util.anonymousMemberDef("perm", false, false), 
				protoField(perm));
		newState.addMember(Util.anonymousMemberDef("type", false, false), 
				protoField(type));
		
		// instantiate the new prototype
		return initAndInstantiateState(PermType.PermType, newState);
	}
	
	private static PlaidObject javaPermToPlaidPerm(plaid.compilerjava.AST.Permission perm) {
		if (perm.equals(Permission.UNIQUE)) {
			return TestUtils.unique();
		}
		else if (perm.equals(Permission.FULL)) {
			return TestUtils.full();
		}
		else if (perm.equals(Permission.IMMUTABLE)) {
			return TestUtils.immutable();
		}
		else if (perm.equals(Permission.SHARED)) {
			return TestUtils.shared();
		}
		else if (perm.equals(Permission.PURE)) {
			return TestUtils.pure();
		}
		else if (perm.equals(Permission.DYN)) {
			return TestUtils.dyn();
		}
		// TODO: add a case for the None permission
		else {
			throw new RuntimeException("Unhandled permission type.");
		}
	}
	
	private static PlaidObject type(plaid.compilerjava.AST.Type structType) {
		Set<plaid.compilerjava.AST.ID> abbrevs = structType.getTypeAbbrevs();
		Set<TypeDecl> typeDecls = structType.getTypeDecls();
		
		// convert the abbrevs and add them
		Set<PlaidObject> plaidAbbrevs = new HashSet<PlaidObject>();
		for (plaid.compilerjava.AST.ID abbrev : abbrevs) {
			plaidAbbrevs.add(TestUtils.id(abbrev.getName()));
		}
		
		// convert the decls and add them
		Set<PlaidObject> plaidTypeDecls = new HashSet<PlaidObject>();
		for (TypeDecl typeDecl : typeDecls) {
			if (typeDecl instanceof MethodTypeDecl) {
				plaidTypeDecls.add(TestUtils.methodType((plaid.compilerjava.AST.MethodTypeDecl)typeDecl));
			}
			else if (typeDecl instanceof FieldTypeDecl) {
				plaidTypeDecls.add(TestUtils.fieldType((plaid.compilerjava.AST.FieldTypeDecl)typeDecl));
			}
			else {
				throw new RuntimeException("Unhandled type decl.");
			}
		}
		
		return TestUtils.type((PlaidObject[])plaidAbbrevs.toArray(), 
							  (PlaidObject[])plaidTypeDecls.toArray());
	}
	
	private static PlaidObject methodType(plaid.compilerjava.AST.MethodTypeDecl decl) {
		String name = decl.getName().getName();
		plaid.compilerjava.AST.PermType retType = decl.getRetPermType();
		List<plaid.compilerjava.AST.PermType> argTypes = decl.getArgTypes();
		
		List<PlaidObject> plaidPermTypes = new ArrayList<PlaidObject>();
		for (plaid.compilerjava.AST.PermType argType : argTypes) {
			plaidPermTypes.add(TestUtils.javaPermTypeToPlaidPermType(argType));
		}
		
		return TestUtils.methodType(TestUtils.id(name), 
									TestUtils.javaPermTypeToPlaidPermType(retType), 
									new PlaidJavaObjectMap(plaidPermTypes),
									new PlaidJavaObjectMap(new ArrayList<PlaidObject>())); // TODO: Fix
	}
	
	private static PlaidObject fieldType(plaid.compilerjava.AST.FieldTypeDecl decl) {
		String name = decl.getName().getName();
		plaid.compilerjava.AST.PermType fieldType = decl.getPermType();
		
		return TestUtils.fieldType(TestUtils.id(name), 
								   TestUtils.javaPermTypeToPlaidPermType(fieldType));
	}
	
	private static PlaidObject memberRepToPlaidType(MemberRep rep) {
		if (rep instanceof MethodRep) {
			MethodRep methodRep = (MethodRep)rep;
			
			plaid.compilerjava.AST.MethodTypeDecl typeDecl = methodRep.getType();
			plaid.compilerjava.AST.PermType retType = typeDecl.getRetPermType();
			List<plaid.compilerjava.AST.PermType> argTypes = typeDecl.getArgTypes();
			List<PlaidObject> plaidArgTypes = new ArrayList<PlaidObject>();
			for (plaid.compilerjava.AST.PermType argType : argTypes) {
				plaidArgTypes.add(TestUtils.javaPermTypeToPlaidPermType(argType));
			}
			
			return TestUtils.methodType(TestUtils.id(methodRep.getName()), 
								 TestUtils.javaPermTypeToPlaidPermType(retType), 
								 new PlaidJavaObjectMap(plaidArgTypes),
								 new PlaidJavaObjectMap(new ArrayList<PlaidObject>())); // TODO: Fix
		}
		else if (rep instanceof FieldRep) {
			FieldRep fieldRep = (FieldRep)rep;
			
			plaid.compilerjava.AST.FieldTypeDecl typeDecl = fieldRep.getType();
			
			return TestUtils.fieldType(TestUtils.id(fieldRep.getName()), 
					TestUtils.javaPermTypeToPlaidPermType(typeDecl.getPermType()));
		}
		else if (rep instanceof StateRep) {
			throw new RuntimeException("State decls not supported yet.");
		}
		else {
			throw new RuntimeException("Unknown MemberRep type.");
		}
	}
}
