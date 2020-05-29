/*
 * Java Genetic Algorithm Library (jenetics-6.0.0).
 * Copyright (c) 2007-2020 Franz Wilhelmstötter
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
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.ext.moea;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 5.2
 * @since 5.2
 */
final class Serial implements Externalizable {

	private static final long serialVersionUID = 1;

	static final byte SIMPLE_INT_VEC = 1;
	static final byte SIMPLE_LONG_VEC = 2;
	static final byte SIMPLE_DOUBLE_VEC = 3;

	/**
	 * The type being serialized.
	 */
	private byte _type;

	/**
	 * The object being serialized.
	 */
	private Object _object;

	/**
	 * Constructor for deserialization.
	 */
	public Serial() {
	}

	/**
	 * Creates an instance for serialization.
	 *
	 * @param type  the type
	 * @param object  the object
	 */
	Serial(final byte type, final Object object) {
		_type = type;
		_object = object;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeByte(_type);
		switch (_type) {
			case SIMPLE_INT_VEC: ((SimpleIntVec)_object).write(out); break;
			case SIMPLE_LONG_VEC: ((SimpleLongVec)_object).write(out); break;
			case SIMPLE_DOUBLE_VEC: ((SimpleDoubleVec)_object).write(out); break;
			default:
				throw new StreamCorruptedException("Unknown serialized type.");
		}
	}

	@Override
	public void readExternal(final ObjectInput in)
		throws IOException
	{
		_type = in.readByte();
		switch (_type) {
			case SIMPLE_INT_VEC: _object = SimpleIntVec.read(in); break;
			case SIMPLE_LONG_VEC: _object = SimpleLongVec.read(in); break;
			case SIMPLE_DOUBLE_VEC: _object = SimpleDoubleVec.read(in); break;
			default:
				throw new StreamCorruptedException("Unknown serialized type.");
		}
	}

	private Object readResolve() {
		return _object;
	}

}
