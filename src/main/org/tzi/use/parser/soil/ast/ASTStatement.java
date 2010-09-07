/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2010 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

// $Id$

package org.tzi.use.parser.soil.ast;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.Token;
import org.tzi.use.parser.AST;
import org.tzi.use.parser.Context;
import org.tzi.use.parser.SemanticException;
import org.tzi.use.parser.SrcPos;
import org.tzi.use.parser.Symtable;
import org.tzi.use.parser.ocl.ASTExpression;
import org.tzi.use.parser.ocl.ASTType;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.expr.VarDeclList;
import org.tzi.use.uml.ocl.type.ObjectType;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.sys.soil.MRValue;
import org.tzi.use.uml.sys.soil.MStatement;
import org.tzi.use.util.soil.SymbolTable;
import org.tzi.use.util.soil.VariableSet;
import org.tzi.use.util.soil.exceptions.compilation.ArgumentTypeMismatchException;
import org.tzi.use.util.soil.exceptions.compilation.AssociationTypeMismatchException;
import org.tzi.use.util.soil.exceptions.compilation.CompilationFailedException;
import org.tzi.use.util.soil.exceptions.compilation.ExpressionGenerationFailedException;
import org.tzi.use.util.soil.exceptions.compilation.IllegalAssociationClassInstantiationException;
import org.tzi.use.util.soil.exceptions.compilation.NotAClassException;
import org.tzi.use.util.soil.exceptions.compilation.NotAStringException;
import org.tzi.use.util.soil.exceptions.compilation.NotATypeException;
import org.tzi.use.util.soil.exceptions.compilation.NotAnAssociationClassException;
import org.tzi.use.util.soil.exceptions.compilation.NotAnAttributeException;
import org.tzi.use.util.soil.exceptions.compilation.NotAnObjectException;
import org.tzi.use.util.soil.exceptions.compilation.NotAnOperationException;
import org.tzi.use.util.soil.exceptions.compilation.ResultTypeMismatch;
import org.tzi.use.util.soil.exceptions.compilation.ResultUnboundException;
import org.tzi.use.util.soil.exceptions.compilation.UnkownAssociationException;
import org.tzi.use.util.soil.exceptions.compilation.VariableTypeUncertainException;
import org.tzi.use.util.soil.exceptions.compilation.VariableUndefinedException;
import org.tzi.use.util.soil.exceptions.compilation.WrongNumOfArgumentsException;
import org.tzi.use.util.soil.exceptions.compilation.WrongNumOfParticipantsException;


/**
 * Base class for objects of the abstract syntax tree
 * generated by ANTLR.
 * <p>
 * Holds references to the {@link Context} {@code fContext} and a special
 * {@link SymbolTable} {@code fSymbolTable}.
 * <p>
 * Furthermore each object of this kind posseses its own 
 * 
 * @author Daniel Gent
 *
 */
public abstract class ASTStatement extends AST {
	/** TODO */
	private SrcPos fSourcePosition;
	/** TODO */
	private String fParsedText;
	/** TODO */
	private List<ASTStatement> fChildStatements = new ArrayList<ASTStatement>();
	/** TODO */
	protected VariableSet fBoundSet = new VariableSet();
	/** TODO */
	protected VariableSet fAssignedSet = new VariableSet();
	/** TODO */
	protected Context fContext;
	/** TODO */
	protected SymbolTable fSymtable;
	/** TODO */
	private Type fRequiredResultType;
	/** TODO */
	protected static boolean VERBOSE = false;
	/** TODO */
	protected static PrintWriter VERBOSE_OUT = new PrintWriter(System.out);

	
	/**
	 * TODO
	 * @return
	 */
	public boolean hasSourcePosition() {
		return fSourcePosition != null;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public SrcPos getSourcePosition() {
		return fSourcePosition;
	}
	
	
	/**
	 * TODO
	 * @param sourcePosition
	 */
	public void setSourcePosition(SrcPos sourcePosition) {
		fSourcePosition = sourcePosition;
	}
	
	
	/**
	 * TODO
	 * @param token
	 */
	public void setSourcePosition(Token token) {
		setSourcePosition(new SrcPos(token));
	}
	
	
	/**
	 * TODO
	 * @param parsedText
	 */
	public void setParsedText(String parsedText) {
		fParsedText = parsedText;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public String getParsedText() {
		return fParsedText;
	}
	
	
	/**
	 * TODO
	 * @param childStatement
	 */
	protected void addChildStatement(ASTStatement childStatement) {
		fChildStatements.add(childStatement);
	}
	
	
	/**
	 * TODO
	 * @param childStatements
	 */
	protected void addChildStatements(List<ASTStatement> childStatements) {
		fChildStatements.addAll(childStatements);
	}

	/**
	 * TODO
	 * @return
	 */
	public List<ASTStatement> getChildStatements() {
		return fChildStatements;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public boolean isEmptyStatement() {
		return (this instanceof ASTEmptyStatement);
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public SymbolTable getSymbolTable() {
		return fSymtable;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public VariableSet bound() {
		return fBoundSet;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public VariableSet assigned() {
		return fAssignedSet;
	}
	
	
	/**
	 * TODO
	 * @param name
	 * @return
	 */
	public boolean binds(String name) {
		return fBoundSet.contains(name);
	}
	
	
	/**
	 * TODO
	 * @param name
	 * @return
	 */
	public boolean assigns(String name) {
		return fAssignedSet.contains(name);
	}
		
	
	/**
	 * TODO
	 * @param type
	 */
	public void mustBindResultAs(Type type) {
		fRequiredResultType = type;
	}
	
	
	/**
	 * TODO
	 * @param verbose
	 */
	public static void setVerbose(boolean verbose) {
		VERBOSE = verbose;
	}
	
	
	/**
	 * TODO
	 * @param output
	 */
	public static void setVerboseOutput(PrintWriter output) {
		VERBOSE_OUT = output;
	}
	
	
	/**
	 * TODO
	 * @param context
	 * @param symtable
	 * @return
	 * @throws CompilationFailedException
	 */
	public MStatement generateStatement(
			Context context,
			SymbolTable symtable) throws CompilationFailedException {
		
		fContext = context;
		fSymtable = symtable;
		
		verbosePrintln("generating " + this);
		
		MStatement result = generateStatement();
	
		result.setSourceStatement(this);
		result.setSourcePosition(fSourcePosition);
		
		if (fRequiredResultType != null) {
			if (!binds("result")) {
				throw new ResultUnboundException(this);
			}
			
			Type resultType = bound().getType("result");
			if (!resultType.isSubtypeOf(fRequiredResultType)) {
				throw new ResultTypeMismatch(
						this, 
						fRequiredResultType, 
						resultType);
			}
		}
		 
		return result;
	}
	
	
	/**
	 * TODO
	 * @param context
	 * @param operation
	 * @return
	 * @throws CompilationFailedException
	 */
	public MStatement generateStatement(
			Context context, 
			MOperation operation) throws CompilationFailedException {
		
		// build symbol table from ...
    	SymbolTable symbolTable = new SymbolTable();
    	// ... parameters and ...
    	for (VarDecl p : operation.allParams()) {
    		symbolTable.setType(p.name(), p.type());
    	}
    	// ... self
    	symbolTable.setType("self", TypeFactory.mkObjectType(operation.cls()));
    	
    	if (operation.hasResultType()) {
    		mustBindResultAs(operation.resultType());
    	}
    	
    	MStatement result = generateStatement(context, symbolTable);
    	result.setIsOperationBody(true);
    	
    	return result;
	}
	
	
	/**
	 * TODO
	 * @param indent
	 * @param indentInc
	 * @param errorOutput
	 */
	public void printTree(PrintWriter target) {
		printTree(new StringBuilder(), target);
	}
	
	
	@Override
	public String toString() {
		return fParsedText;
	}
	
	
	/**
	 * TODO
	 * @param prelude
	 * @param target
	 */
	protected abstract void printTree(
			StringBuilder prelude, 
			PrintWriter target);
	
	/**
	 * TODO
	 */
	public void flatten() {
		for (ASTStatement statement : fChildStatements) {
			statement.flatten();
		}
	}
	

	/**
	 * TODO
	 * @param output
	 */
	protected void verbosePrint(Object output) {
		if (VERBOSE) {
			VERBOSE_OUT.print(output);
			VERBOSE_OUT.flush();
		}
	}
	
	
	/**
	 * TODO
	 * @param output
	 */
	protected void verbosePrintln(Object output) {
		verbosePrint(output + "\n");
	}
	

	/**
	 * TODO
	 * @return
	 * @throws CompilationFailedException
	 */
	protected abstract MStatement generateStatement() throws CompilationFailedException;

	
	/**
	 * TODO
	 * @param expression
	 * @param context
	 * @param symbolTable
	 * @return
	 * @throws CompilationFailedException
	 */
	private Expression generateExpression(
			ASTExpression expression,
			Context context,
			SymbolTable symbolTable) throws CompilationFailedException {
	
		Set<String> freeVariables = expression.getFreeVariables();
		
		Symtable newSymtable = new Symtable();
		for (String name : freeVariables) {
			if (!symbolTable.contains(name)) {
				throw new VariableUndefinedException(
						this, 
						expression, 
						name);
			}
			
			if (symbolTable.isDirty(name)) {
				throw new VariableTypeUncertainException(
						this, 
						expression, 
						name, 
						symbolTable.getCause(name));
			}
			
			try {
				newSymtable.add(name, symbolTable.getType(name), null);
			} catch (SemanticException e) {
				// reason for the caught exception is adding a variable with a name
				// that's already present. since we're adding the keys of a hashmap
				// to an empty Symtable this can't happen.
				// should this behavior change in the future, here's a hint to those
				// who broke it...
				throw new RuntimeException("unexpected exception", e);
			}
		}
		
		// if this statement gets parsed as part of an operation definition, we could
		// possibly break it by fiddling around with the symtable. so let's just make sure
		// the caller gets it back the way it was.
		Symtable backup = context.varTable();
		context.setVarTable(newSymtable);
		try {
			return expression.gen(context);
		} catch (SemanticException e) {
			throw new ExpressionGenerationFailedException(
					this, 
					expression, 
					e);
		} finally {
			context.setVarTable(backup);
		}
	}
	
	
	/**
	 * TODO
	 * @param expression
	 * @return
	 * @throws CompilationFailedException
	 */
	protected Expression generateExpression(
			ASTExpression expression) throws CompilationFailedException {
		
		return generateExpression(expression, fContext, fSymtable);
	}
	
	
	
	/**
	 * TODO
	 * @param expression
	 * @return
	 * @throws CompilationFailedException
	 */
	protected Expression generateObjectExpression(
			ASTExpression expression) throws CompilationFailedException {
		
		Expression possibleObject = generateExpression(expression);
		
		if (!possibleObject.type().isObjectType()) {
			throw new NotAnObjectException(this, possibleObject);
		}
		
		return possibleObject;
	}
	
	
	/**
	 * TODO
	 * @param expression
	 * @return
	 * @throws CompilationFailedException
	 */
	protected Expression generateStringExpression(
			ASTExpression expression) throws CompilationFailedException {
		
		Expression possibleString = generateExpression(expression);
		
		if (!possibleString.type().isString()) {
			throw new NotAStringException(
					this, 
					expression, 
					possibleString.type());
		}
		
		return possibleString;
	}
	
	
	/**
	 * TODO
	 * @param object
	 * @param attributeName
	 * @return
	 * @throws CompilationFailedException
	 */
	protected MAttribute generateAttribute(
			Expression object, 
			String attributeName) throws CompilationFailedException {
		
		if (!object.type().isObjectType()) {
			throw new NotAnObjectException(this, object);
		}
		
		MClass objectClass = ((ObjectType)object.type()).cls();
		MAttribute attribute = 
			objectClass.attribute(attributeName, true);
		
		if (attribute == null) {
			throw new NotAnAttributeException(
					this, 
					objectClass, 
					attributeName);
		}
		
		return attribute;
	}
	
	
	/**
	 * TODO
	 * @param statement
	 * @return
	 * @throws CompilationFailedException 
	 */
	protected MStatement generateStatement(
			ASTStatement statement) throws CompilationFailedException {
		
		return statement.generateStatement(fContext, fSymtable);
	}
	
	
	/**
	 * TODO
	 * @param type
	 * @return
	 * @throws NotATypeException
	 */
	protected Type generateType(
			ASTType type) throws NotATypeException {

		try {
			return type.gen(fContext);
		} catch(SemanticException e) {
			throw new NotATypeException(this, type);
		}			
	}
	
	
	/**
	 * TODO
	 * @param type
	 * @return
	 * @throws CompilationFailedException
	 */
	protected MClass generateClass(
			ASTType type,
			boolean mayBeAssociationClass) throws CompilationFailedException {
		
		Type t = generateType(type);
		
		if (!t.isObjectType()) {
			throw new NotAClassException(this, t);
		}
		
		MClass result = ((ObjectType)t).cls();
		
		if (!mayBeAssociationClass && 
				fContext.model().associations().contains(result)) {
			
			throw new IllegalAssociationClassInstantiationException(
					this, 
					result);
		}
		
		return result;
	}
	
	
	/**
	 * TODO
	 * @param name
	 * @return
	 * @throws UnkownAssociationException
	 */
	protected MAssociation generateAssociation(
			String name) throws UnkownAssociationException {
		
		MAssociation association = 
			fContext.model().getAssociation(name);
		
		if (association == null) {
			
			throw new UnkownAssociationException(
					this, 
					name);
		}
		
		return association;
	}
	
	
	/**
	 * TODO
	 * @param name
	 * @return
	 * @throws NotAnAssociationClassException 
	 */
	protected MAssociationClass generateAssociationClass(
			String name) throws NotAnAssociationClassException {
		
		MAssociationClass result =
            fContext.model().getAssociationClass(name);
		
		if (result == null) {
			
			throw new NotAnAssociationClassException(
					this, 
					name);
		}
			
		return result;
	}
	
	
	/**
	 * TODO
	 * @param association
	 * @param participants
	 * @return
	 * @throws CompilationFailedException
	 */
	protected List<MRValue> generateAssociationParticipants(
			MAssociation association, 
			List<ASTRValue> participants) throws CompilationFailedException
	{
		
		List<MAssociationEnd> associationEnds = 
			association.associationEnds();
		
		int numParticipants = participants.size();
		int numAssociationEnds = associationEnds.size();
		
		if (numAssociationEnds != numParticipants) {
			
			throw new WrongNumOfParticipantsException(
					this, 
					association,
					numAssociationEnds,
					numParticipants);
		}
		
		// generate the participant for each slot, and check
		// if their respective types are compatible
		List<MRValue> result = 
			new ArrayList<MRValue>(numParticipants);	
		
		for (int i = 0; i < numParticipants; ++i)	
		{
			MAssociationEnd associationEnd = 
				associationEnds.get(i);
			
			MRValue participant = 
				generateRValue(participants.get(i));
			
			Type expectedType = associationEnd.cls().type();
			Type foundType = participant.getType();
			
			if (!foundType.isSubtypeOf(expectedType)) {
				
				throw new AssociationTypeMismatchException(
						this, 
						association, 
						participants.get(i),
						expectedType,
						foundType,
						i);	
			}
			
			result.add(participant);
		}
		
		return result;
	}
	
	
	/**
	 * TODO
	 * @param object
	 * @param operationName
	 * @return
	 * @throws CompilationFailedException
	 */
	protected MOperation generateOperation(
			Expression object, 
			String operationName) throws CompilationFailedException {
		
		if (!object.type().isObjectType()) {
			throw new NotAnObjectException(this, object);
		}
		
		MClass objectClass = ((ObjectType)object.type()).cls();
		
		MOperation result = objectClass.operation(operationName, true);
		if (result == null) {
			
			throw new NotAnOperationException(
					this, 
					objectClass.name(), 
					operationName);
		}
		
		return result;
	}
	
	
	/**
	 * TODO
	 * @param objectClass
	 * @param operationName
	 * @return
	 * @throws NotAnOperationException
	 */
	protected MOperation generateOperation(
			MClass objectClass, 
			String operationName) throws NotAnOperationException {
		
		MOperation result = objectClass.operation(operationName, true);
		
		if (result == null) {
			
			throw new NotAnOperationException(
					this, 
					objectClass.name(), 
					operationName);
		}
		
		return result;
	}
	
	
	/**
	 * TODO
	 * @param operation
	 * @param arguments
	 * @return
	 * @throws CompilationFailedException
	 */
	protected LinkedHashMap<String, Expression> generateOperationArguments(
			MOperation operation,
			List<ASTExpression> arguments) throws CompilationFailedException {
		
		VarDeclList parameters = operation.paramList();
		
		int numParameters = parameters.size();
		int numArguments = arguments.size();
		
		if (numParameters != numArguments) {
			
			throw new WrongNumOfArgumentsException(
					this, 
					operation, 
					arguments);
		}
		
		LinkedHashMap<String, Expression> result = 
			new LinkedHashMap<String, Expression>(arguments.size());
		
		for (int i = 0; i < numParameters; ++i) {
			
			VarDecl parameter = parameters.varDecl(i);
			Expression argument = generateExpression(arguments.get(i));
			
			Type expectedType = parameter.type();
			Type foundType = argument.type();
			
			if (!foundType.isSubtypeOf(expectedType)) {
				
				throw new ArgumentTypeMismatchException(
						this, 
						operation,
						arguments.get(i),
						expectedType,
						foundType,
						i);
			}
			
			result.put(parameter.name(), argument);
		}
		
		return result;
	}
	
	
	/**
	 * TODO
	 * @param rValue
	 * @return
	 * @throws CompilationFailedException
	 */
	protected MRValue generateRValue(
			ASTRValue rValue) throws CompilationFailedException {
		
		return rValue.generate(this);
	}
}