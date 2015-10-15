/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core;

/**
 * An object (workflow instance of reponse) with the same id already exists
 *
 * @author austermann
 */
public class DuplicateIdException extends CopperRuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateIdException() {
        super();
    }

    public DuplicateIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateIdException(String message) {
        super(message);
    }

    public DuplicateIdException(Throwable cause) {
        super(cause);
    }

}
