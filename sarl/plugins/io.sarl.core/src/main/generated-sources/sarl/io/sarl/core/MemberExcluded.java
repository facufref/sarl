/**
 * $Id$
 * 
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 * 
 * Copyright (C) 2014-2016 the original authors or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.core;

import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Event;
import java.util.UUID;
import javax.annotation.Generated;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * Notifies other members the member with agentID was
 * excluded (forced to leave) the holon context
 * parentContextID
 */
@SarlSpecification("0.4")
@SuppressWarnings("all")
public class MemberExcluded extends Event {
  public final UUID parentContextID;
  
  public final UUID agentID;
  
  public final String agentType;
  
  public MemberExcluded(final Address src, final UUID pid, final UUID aid, final String at) {
    this.setSource(src);
    this.parentContextID = pid;
    this.agentID = aid;
    this.agentType = at;
  }
  
  @Override
  @Pure
  @Generated("io.sarl.lang.jvmmodel.SARLJvmModelInferrer")
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MemberExcluded other = (MemberExcluded) obj;
    if (this.parentContextID == null) {
      if (other.parentContextID != null)
        return false;
    } else if (!this.parentContextID.equals(other.parentContextID))
      return false;
    if (this.agentID == null) {
      if (other.agentID != null)
        return false;
    } else if (!this.agentID.equals(other.agentID))
      return false;
    if (this.agentType == null) {
      if (other.agentType != null)
        return false;
    } else if (!this.agentType.equals(other.agentType))
      return false;
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @Generated("io.sarl.lang.jvmmodel.SARLJvmModelInferrer")
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.parentContextID== null) ? 0 : this.parentContextID.hashCode());
    result = prime * result + ((this.agentID== null) ? 0 : this.agentID.hashCode());
    result = prime * result + ((this.agentType== null) ? 0 : this.agentType.hashCode());
    return result;
  }
  
  /**
   * Returns a String representation of the MemberExcluded event's attributes only.
   */
  @Generated("io.sarl.lang.jvmmodel.SARLJvmModelInferrer")
  @Pure
  protected String attributesToString() {
    StringBuilder result = new StringBuilder(super.attributesToString());
    result.append("parentContextID  = ").append(this.parentContextID);
    result.append("agentID  = ").append(this.agentID);
    result.append("agentType  = ").append(this.agentType);
    return result.toString();
  }
  
  @Generated("io.sarl.lang.jvmmodel.SARLJvmModelInferrer")
  private final static long serialVersionUID = 1318270431L;
}
