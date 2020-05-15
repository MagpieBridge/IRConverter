/*
 * @author Linghui Luo
 */
package magpiebridge.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.RefLikeType;
import soot.ShortType;
import soot.Type;
import soot.UnknownType;
import soot.VoidType;
import soot.jimple.Jimple;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2004 Jennifer Lhotak
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * Generates locals for Body.
 *
 * @author Linghui Luo
 * @author Markus Schmidt
 */
public class LocalGenerator {
  private final Set<Local> locals;
  @Nullable private Local thisLocal;
  private final Map<Integer, Local> paraLocals = new HashMap<>();

  /**
   * Creates Locals {@link Local} with a standard naming scheme. If a Set of Locals is provided, the
   * LocalGenerator checks whether the name is already taken.
   *
   * @param existingLocals the existing locals
   */
  public LocalGenerator(@Nonnull Set<Local> existingLocals) {
    locals = existingLocals;
  }

  /**
   * Generate local for this reference with given type.
   *
   * @param type the type
   * @return the local
   */
  public Local generateThisLocal(@Nonnull Type type) {
    if (this.thisLocal == null) {
      this.thisLocal = generateField(type);
    }
    return this.thisLocal;
  }

  /**
   * generates a new {@link Local} given the type for field.
   *
   * @param type the type
   * @return the local
   */
  public Local generateField(@Nonnull Type type) {
    return generate(type, true);
  }

  /**
   * generates a new {@link Local} given the type for local.
   *
   * @param type the type
   * @return the local
   */
  public Local generateLocal(@Nonnull Type type) {
    return generate(type, false);
  }

  public Local generateParameterLocal(@Nonnull Type type, int index) {
    if (!this.paraLocals.containsKey(index)) {
      Local paraLocal = generate(type, false);
      this.paraLocals.put(index, paraLocal);
    }
    return this.paraLocals.get(index);
  }

  private Local generate(@Nonnull Type type, boolean isField) {
    StringBuilder name = new StringBuilder(7);
    name.append("$");
    String localName;
    // determine locals name
    //noinspection SuspiciousMethodCalls
    do {
      // non-field Locals traditionally begin with "$"
      name.setLength(isField ? 0 : 1);

      if (type.equals(IntType.v())) {
        appendNextIntName(name);
      } else if (type.equals(ByteType.v())) {
        appendNextByteName(name);
      } else if (type.equals(ShortType.v())) {
        appendNextShortName(name);
      } else if (type.equals(BooleanType.v())) {
        appendNextBooleanName(name);
      } else if (type.equals(VoidType.v())) {
        appendNextVoidName(name);
      } else if (type.equals(CharType.v())) {
        appendNextCharName(name);
      } else if (type.equals(DoubleType.v())) {
        appendNextDoubleName(name);
      } else if (type.equals(FloatType.v())) {
        appendNextFloatName(name);
      } else if (type.equals(LongType.v())) {
        appendNextLongName(name);
      } else if (type instanceof RefLikeType) {
        appendNextRefLikeTypeName(name);
      } else if (type.equals(UnknownType.v())) {
        appendNextUnknownTypeName(name);
      } else {
        throw new RuntimeException(
            "Unhandled Type of Local variable to Generate: " + type.toString());
      }

      localName = name.toString();
    } while (containsLocal(localName));

    return createLocal(localName, type);
  }

  private boolean containsLocal(String name) {
    for (Local l : locals)
      if (l.toString().equals(name)) {
        return true;
      }
    return false;
  }

  private int tempInt = 0;
  private int tempVoid = 0;
  private int tempBoolean = 0;
  private int tempLong = 0;
  private int tempDouble = 0;
  private int tempFloat = 0;
  private int tempRefLikeType = 0;
  private int tempByte = 0;
  private int tempShort = 0;
  private int tempChar = 0;
  private int tempUnknownType = 0;

  private void appendNextIntName(StringBuilder name) {
    name.append("i").append(tempInt++);
  }

  private void appendNextCharName(StringBuilder name) {
    name.append("c").append(tempChar++);
  }

  private void appendNextVoidName(StringBuilder name) {
    name.append("v").append(tempVoid++);
  }

  private void appendNextByteName(StringBuilder name) {
    name.append("b").append(tempByte++);
  }

  private void appendNextShortName(StringBuilder name) {
    name.append("s").append(tempShort++);
  }

  private void appendNextBooleanName(StringBuilder name) {
    name.append("z").append(tempBoolean++);
  }

  private void appendNextDoubleName(StringBuilder name) {
    name.append("d").append(tempDouble++);
  }

  private void appendNextFloatName(StringBuilder name) {
    name.append("f").append(tempFloat++);
  }

  private void appendNextLongName(StringBuilder name) {
    name.append("l").append(tempLong++);
  }

  private void appendNextRefLikeTypeName(StringBuilder name) {
    name.append("r").append(tempRefLikeType++);
  }

  private void appendNextUnknownTypeName(StringBuilder name) {
    name.append("u").append(tempUnknownType++);
  }

  private Local createLocal(String name, Type sootType) {
    Local sootLocal = Jimple.v().newLocal(name, sootType);
    locals.add(sootLocal);
    return sootLocal;
  }

  /**
   * Return all locals created for the body referenced in this LocalGenrator.
   *
   * @return the locals
   */
  public Set<Local> getLocals() {
    return this.locals;
  }

  public Local getThisLocal() {
    return this.thisLocal;
  }

  public Local getParameterLocal(int i) {
    return this.paraLocals.get(i);
  }
}
