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

import static java.lang.String.format;

import java.util.List;

import io.jenetics.Optimize;

/**
 * Some vector helper methods.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 5.2
 * @since 4.1
 */
final class Vecs {

	private Vecs() {
	}

	static void checkVecLength(final int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("Array length must greater zero.");
		}
	}

	static void requireVecLength(final int expected, final int length) {
		if (expected != length) {
			throw new IllegalArgumentException(format(
				"Expected Vec.length of %d, but got %d.",
				expected, length
			));
		}
	}

	static boolean[] toFlags(final List<Optimize> optimizes) {
		final boolean[] flags = new boolean[optimizes.size()];
		for (int i = 0; i < optimizes.size(); ++i) {
			flags[i] = optimizes.get(i) == Optimize.MAXIMUM;
		}
		return flags;
	}

}
