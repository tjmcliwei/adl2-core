/*
 * ADL2-core
 * Copyright (c) 2013-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of ADL2-core.
 *
 * ADL2-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openehr.adl.util;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.SerializationUtils;
import org.openehr.jaxb.am.Archetype;
import org.openehr.jaxb.am.CObject;
import org.openehr.jaxb.am.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author markopi
 */
public class AdlUtils {
    private static final String AM_PACKAGE_NAME = Archetype.class.getPackage().getName();

    private AdlUtils() {
    }

    public static <T> Collection<T> emptyIfNull(@Nullable Collection<T> details) {
        if (details == null) return Collections.emptyList();
        return details;
    }

    public static String getRmTypeName(@Nonnull Class<?> clazz) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, clazz.getSimpleName());
    }


    public static Class<? extends CObject> getAmClass(@Nonnull String amTypeName) throws ClassNotFoundException {
        int genericsTypeIndex = amTypeName.indexOf('<');
        String name = genericsTypeIndex == -1 ? amTypeName : amTypeName.substring(0, genericsTypeIndex);
        String className = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
        //noinspection unchecked
        return (Class<CObject>) Class.forName(AM_PACKAGE_NAME + '.' + className);
    }


    @Nullable
    public static Float doubleToFloat(@Nullable Double d) {
        return d != null ? d.floatValue() : null;
    }

    public static <T> List<T> front(List<T> list) {
        return list.subList(0, list.size() - 1);
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T head(List<T> list) {
        return list.get(0);
    }

    public static <T> List<T> tail(List<T> list) {
        return list.subList(1, list.size());
    }

    /**
     * Creates a clone using java serialization
     *
     * @param from Object to be cloned
     * @param <T>  type of the cloned object
     * @return Clone of the object
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T makeClone(T from) {
        return (T) SerializationUtils.clone(from);
    }


    public static Archetype createFlatArchetypeClone(Archetype source) {
        checkArgument(source.isIsDifferential(), "source archetype must be a differential archetype");
        Archetype result = makeClone(source);
        result.setIsDifferential(false);
        return result;
    }

    public static Archetype createDifferentialArchetypeClone(Archetype source) {
        checkArgument(!source.isIsDifferential(), "source archetype must be a flat archetype");
        Archetype result = makeClone(source);
        result.setIsDifferential(true);
        return result;
    }


    public static void fillArchetypeFields(Archetype target, Archetype source) {
        target.setIsControlled(source.isIsControlled());
        target.setBuildUid(source.getBuildUid());
        target.setAdlVersion(source.getAdlVersion());
        target.setDescription(source.getDescription());
        target.setOriginalLanguage(source.getOriginalLanguage());
        target.setRevisionHistory(source.getRevisionHistory());
        target.setAnnotations(source.getAnnotations());
        target.setIsGenerated(source.isIsGenerated());
        target.setRmRelease(source.getRmRelease());

        target.getOtherMetadata().addAll(source.getOtherMetadata());

        target.getTranslations().addAll(source.getTranslations());

        target.setDefinition(source.getDefinition());
        target.setArchetypeId(source.getArchetypeId());
        target.setIsDifferential(source.isIsDifferential());
        target.setConcept(source.getConcept());
        target.setTerminology(source.getTerminology());
        target.setParentArchetypeId(source.getParentArchetypeId());

        target.getInvariants().addAll(source.getInvariants());
        target.setIsOverlay(source.isIsOverlay());
        target.setIsTemplate(source.isIsTemplate());
    }


    public static Template buildTemplate(List<Archetype> archetypes) {
        Template result = new Template();
        Archetype first = archetypes.get(0);

        AdlUtils.fillArchetypeFields(result, first);
        for (Archetype archetype : Iterables.skip(archetypes, 1)) {
            result.getOverlays().add(archetype);
        }

        return result;
    }

    public static List<Archetype> demolishTemplate(Template template) {
        List<Archetype> result = new ArrayList<>();
        Archetype first = new Archetype();
        AdlUtils.fillArchetypeFields(first, template);
        result.add(first);

        result.addAll(template.getOverlays());

        return result;
    }


}
