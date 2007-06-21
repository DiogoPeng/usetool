/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
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

/* $ProjectHeader: use 0.393 Wed, 16 May 2007 14:10:28 +0200 opti $ */

package org.tzi.use.parser.cmd;

import java.util.Iterator;
import java.util.List;

import org.tzi.use.parser.Context;
import org.tzi.use.parser.MyToken;
import org.tzi.use.parser.SemanticException;
import org.tzi.use.parser.ocl.ASTExpression;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.sys.MCmd;
import org.tzi.use.uml.sys.MCmdInsertLink;

/**
 * Node of the abstract syntax tree constructed by the parser.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author  Mark Richters
 */
public class ASTInsertCmd extends ASTCmd {
    private List fExprList; // (ASTExpression)
    private MyToken fAssocName;

    public ASTInsertCmd(List exprList, MyToken assocName) {
        fExprList = exprList;
        fAssocName = assocName;
    }

    public MCmd gen(Context ctx) 
        throws SemanticException
    {
        MAssociation assoc = 
            ctx.model().getAssociation(fAssocName.getText());
        if (assoc == null )
            throw new SemanticException(fAssocName, "Association `" + 
                                        fAssocName.getText() + "' does not exist.");

        if (assoc.associationEnds().size() != fExprList.size() )
            throw new SemanticException(fAssocName, "A link for association `" + 
                                        fAssocName.getText() + "' requires " + assoc.associationEnds().size() +
                                        " objects, found " + fExprList.size() + ".");

        // generate expressions
        Expression[] exprs = new Expression[fExprList.size()];
        Iterator it = fExprList.iterator();
        int i = 0;
        while (it.hasNext() ) {
            ASTExpression astExpr = (ASTExpression) it.next();
            exprs[i++] = astExpr.gen(ctx);
        }
        return new MCmdInsertLink(ctx.systemState(), exprs, assoc);
    }
}