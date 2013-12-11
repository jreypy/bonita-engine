/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.theme.builder;

import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public interface SThemeBuilderFactory {

    static String ID = "id";

    static String CONTENT = "content";

    static String CSS_CONTENT = "cssContent";

    static String LAST_UPDATE_DATE = "lastUpdateDate";

    static String TYPE = "type";

    static String IS_DEFAULT = "isDefault";

    SThemeBuilder createNewInstance(STheme theme);

    SThemeBuilder createNewInstance(byte[] content, boolean isDefault, SThemeType type, long lastUpdateDate);

}
