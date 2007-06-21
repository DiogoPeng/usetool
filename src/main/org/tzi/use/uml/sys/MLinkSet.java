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

package org.tzi.use.uml.sys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;

/**
 * A link set contains instances of an association.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author      Mark Richters 
 */
public final class MLinkSet {
    private final MAssociation fAssociation; // The type of all links of this set.
    private Set fLinks;     // (MLink)

    private static class CacheEntry {
        final MAssociationEnd end;
        final MObject         object;
        final int hashCode;
        public CacheEntry(MAssociationEnd end, MObject object) {
            this.end = end;
            this.object = object;
            hashCode = end.hashCode() + 19 * object.hashCode();
        }
        public final int hashCode() {
            return hashCode;
        }
        public final boolean equals(Object o) {
            CacheEntry e = (CacheEntry)o;
            return e.end == end && e.object == object;
        }
    }
    
    private Map selectCache; // CacheEntry -> Set 
    
    MLinkSet(MAssociation assoc) {
        fAssociation = assoc;
        fLinks = new HashSet();
        selectCache = new HashMap();
    }

    /**
     * Copy constructor.
     */
    MLinkSet(MLinkSet x) {
        fAssociation = x.fAssociation;
        fLinks = new HashSet();
        fLinks.addAll(x.fLinks);
        selectCache = new HashMap();
        for (Iterator it = x.fLinks.iterator(); it.hasNext();) {
            MLink link = (MLink) it.next();
            add(link);
        }
    }

    /**
     * Selects all links whose link ends at <code>aend</code> connect
     * <code>obj</code>. 
     *
     * @return Set(MLink)
     */
    Set select(MAssociationEnd aend, MObject obj) {
        Set res;
        if (selectCache!=null) {
            CacheEntry e = new CacheEntry(aend, obj);
            res = (Set) selectCache.get(e);
            if (res==null) res = new HashSet();
            return res;
        }
        res = new HashSet();

        Iterator it = fLinks.iterator();
        while (it.hasNext() ) {
            MLink link = (MLink) it.next();
            MLinkEnd linkEnd = link.linkEnd(aend);
            if (linkEnd.object().equals(obj) )
                res.add(link);
        }
        return res;
    }

    /**
     * Removes all links whose link ends at <code>aend</code> connect
     * <code>obj</code>. 
     *
     * @return Set(MLink) the set of removed links
     */
    Set removeAll(MAssociationEnd aend, MObject obj) {
        Set res = new HashSet();
        Iterator it = fLinks.iterator();
        while (it.hasNext() ) {
            MLink link = (MLink) it.next();
            MLinkEnd linkEnd = link.linkEnd(aend);
            if (linkEnd.object().equals(obj) ) {
                res.add(link);
                it.remove();
                CacheEntry e = new CacheEntry(aend, obj);
                if (selectCache.containsKey(e)) selectCache.remove(e);
            }
        }
        return res;
    }


    /**
     * Returns the association describing this link set.
     */
    public MAssociation association() {
        return fAssociation;
    }

    /**
     * Returns the number of links in this set.
     */
    public int size() {
        return fLinks.size();
    }

    /**
     * Returns the set of links in this set.
     *
     * @return Set(MLink)
     */
    public Set links() {
        return fLinks;
    }

    /**
     * Checks whether a link is in this set.
     *
     * @return true if the link is in this set.
     */
    boolean contains(MLink link) {
        return fLinks.contains(link);
    }

    /**
     * Adds a link to this link set.
     *
     * @return true if the link set did not already contain the link.
     */
    boolean add(MLink link) {
        for (Iterator it = link.linkEnds().iterator(); it.hasNext();) {
            MLinkEnd end = (MLinkEnd) it.next();
            CacheEntry e = new CacheEntry(end.associationEnd(), end.object());
            Set links = (Set) selectCache.get(e);
            if (links==null) {
                links = new HashSet();
                selectCache.put(e, links);
            }
            links.add(link);
        }
        return fLinks.add(link);
    }

    /**
     * Returns true if there is a link connecting the given set of
     * objects.  
     */
    public boolean hasLinkBetweenObjects(MObject[] objects) {
        Iterator linkIter = fLinks.iterator();
        while (linkIter.hasNext() ) {
            MLink link = (MLink) linkIter.next();
            if (Arrays.equals(link.linkedObjectsAsArray(),objects)) return true;
        }
        return false;
    }

    /**
     * Returns the link if there is a link connecting the given set of
     * objects, otherwise null is returend.  
     */
    public MLink linkBetweenObjects(Set objects) {
        Iterator linkIter = fLinks.iterator();
        while (linkIter.hasNext() ) {
            MLink link = (MLink) linkIter.next();
            if (link.linkedObjects().equals(objects) )
                return link;
        }
        return null;
    }
    
    /**
     * Returns true if there is a link connecting the objects
     * in the given sequence.
     */
    public boolean hasLink(List objects) throws MSystemException {
        return contains(new MLinkImpl(fAssociation, objects));
    }

    /**
     * Removes a link from this link set.
     *
     * @return true if the link set did contain the link.
     */
    boolean remove(MLink link) {
        for (Iterator it = link.linkEnds().iterator(); it.hasNext();) {
            MLinkEnd end = (MLinkEnd) it.next();
            CacheEntry e = new CacheEntry(end.associationEnd(), end.object());
            Set links = (Set) selectCache.get(e);
            if (links!=null) {
                links.remove(link);
                if (links.isEmpty()) {
                    selectCache.remove(e);
                }
            }
        }
        return fLinks.remove(link);
    }

    //      /**
    //       * Dump all information about this link set to stdout.
    //       */
    //      public void dump() {
    //      Iterator it = fLinks.iterator();
    //      while (it.hasNext() ) {
    //          MLink link = (MLink) it.next();
    //          System.out.println(link);
    //      }
    //      }


}
