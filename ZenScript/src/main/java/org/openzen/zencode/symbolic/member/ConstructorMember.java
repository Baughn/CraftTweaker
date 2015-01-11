/*
 * This file is part of MineTweaker API, licensed under the MIT License (MIT).
 * 
 * Copyright (c) 2014 MineTweaker <http://minetweaker3.powerofbytes.com>
 */
package org.openzen.zencode.symbolic.member;

import java.util.List;
import org.openzen.zencode.parser.member.ParsedConstructor;
import org.openzen.zencode.symbolic.Modifier;
import org.openzen.zencode.symbolic.annotations.SymbolicAnnotation;
import org.openzen.zencode.symbolic.method.MethodHeader;
import org.openzen.zencode.symbolic.definition.ISymbolicDefinition;
import org.openzen.zencode.symbolic.statement.Statement;
import org.openzen.zencode.symbolic.expression.IPartialExpression;
import org.openzen.zencode.symbolic.scope.IMethodScope;
import org.openzen.zencode.symbolic.scope.IDefinitionScope;
import org.openzen.zencode.symbolic.scope.MethodScope;

/**
 *
 * @author Stan
 * @param <E>
 */
public class ConstructorMember<E extends IPartialExpression<E>> implements IMember<E>
{	
	private final ParsedConstructor source;
	private final IMethodScope<E> methodScope;
	private final int modifiers;
	
	private Statement<E> contents;
	private List<SymbolicAnnotation<E>> annotations;
	
	public ConstructorMember(IDefinitionScope<E> scope, int modifiers, MethodHeader<E> header, Statement<E> contents)
	{
		source = null;
		this.contents = contents;
		methodScope = new MethodScope<E>(scope, header);
		this.modifiers = modifiers;
	}
	
	public ConstructorMember(ParsedConstructor source, IDefinitionScope<E> scope)
	{
		this.source = source;
		
		MethodHeader<E> definedHeader = source.getSignature().compile(scope);
		if (definedHeader.getReturnType() != null)
			scope.getErrorLogger().errorConstructorHasReturnType(definedHeader.getPosition());
		
		MethodHeader<E> actualHeader = new MethodHeader<E>(
				definedHeader.getPosition(),
				definedHeader.getGenericParameters(),
				scope.getTypeCompiler().getVoid(scope),
				definedHeader.getParameters(),
				definedHeader.isVarargs());
		
		methodScope = new MethodScope<E>(scope, actualHeader);
		modifiers = Modifier.compileModifiers(source.getModifiers(), scope.getErrorLogger());
	}
	
	// #################################
	// ### Implementation of IMember ###
	// #################################

	@Override
	public ISymbolicDefinition<E> getUnit()
	{
		return methodScope.getDefinition();
	}

	@Override
	public void completeContents()
	{
		methodScope.getMethodHeader().completeContents(methodScope);
		
		if (source != null)
		{
			contents = source.getContents().compile(methodScope);
			annotations = SymbolicAnnotation.compileAll(source.getAnnotations(), methodScope);
		}
	}

	@Override
	public void validate()
	{
		methodScope.getMethodHeader().validate(methodScope);
		contents.validate();
	}

	@Override
	public int getModifiers()
	{
		return modifiers;
	}

	@Override
	public List<SymbolicAnnotation<E>> getAnnotations()
	{
		return annotations;
	}
}