/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.identity.model.builder;

import org.bonitasoft.engine.identity.model.SRole;

/**
 * @author Baptiste Mesta
 * @author Bole Zhang
 * @author Bole Zhang
 */
public interface SRoleBuilder {

    SRoleBuilder setId(final long id);

    SRoleBuilder setName(final String name);

    SRoleBuilder setDisplayName(final String displayName);

    SRoleBuilder setDescription(final String description);

    SRoleBuilder setIconName(final String iconName);

    SRoleBuilder setIconPath(final String iconPath);

    SRoleBuilder setCreatedBy(final long createdBy);

    SRoleBuilder setCreationDate(final long creationDate);

    SRoleBuilder setLastUpdate(final long lastUpdate);

    SRole done();

}
