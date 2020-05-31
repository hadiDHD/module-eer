/*
 * PRNGine - Java PRNG Library (prngine-1.0.2).
 * Copyright (c) 2017-2019 Franz Wilhelmstötter
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
package io.jenetics.prngine;

import static java.lang.Math.min;

import java.util.Objects;
import java.util.Random;
import java.util.function.LongSupplier;

/**
 * An abstract base class which eases the implementation of {@code Random}
 * objects which natively creates random {@code long} values. All other
 * {@code Random} functions are optimized using this {@code long} values.
 * Subtypes of this class must only implement the {@link #nextLong()} method.
 * <pre>{@code
 * final class MyRandom extends Random54 {
 *     public int nextLong() {
 *         return ...;
 *     }
 * }
 * }</pre>
 *
 * A quicker way for creating a {@code Random64} instance is using the static
 * factory method {@link #of(LongSupplier)}.
 * <pre>{@code
 * final Random jrand = new Random();
 * final Random64 random = Random64.of(jrand::nextLong);
 * }</pre>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 1.0
 */
public abstract class Random64 extends PRNG {

	private static final long serialVersionUID = 1L;

	protected Random64(final long seed) {
		super(seed);
	}

	protected Random64() {
		this(PRNG.seed());
	}

	/**
	 * Force to explicitly override the Random.nextLong() method. All other
	 * methods of this class are implemented by calling this method.
	 */
	@Override
	public abstract long nextLong();


	@Override
	public boolean nextBoolean() {
		return (nextLong() & 0x8000000000000000L) != 0L;
	}

	@Override
	public int nextInt() {
		return (int)(nextLong() >>> Integer.SIZE);
	}

	@Override
	protected int next(final int bits) {
		return (int)(nextLong() >>> (Long.SIZE - bits));
	}

	/**
	 * Optimized version of the {@link Random#nextBytes(byte[])} method for
	 * 64-bit random engines.
	 */
	@Override
	public void nextBytes(final byte[] bytes) {
		for (int i = 0, len = bytes.length; i < len;) {
			int n = min(len - i, Long.BYTES);

			for (long x = nextLong(); --n >= 0; x >>= Byte.SIZE) {
				bytes[i++] = (byte)x;
			}
		}
	}

	@Override
	public float nextFloat() {
		return PRNG.toFloat2(nextLong());
	}

	/**
	 * Optimized version of the {@link Random#nextDouble()} method for 64-bit
	 * random engines.
	 */
	@Override
	public double nextDouble() {
		return PRNG.toDouble2(nextLong());
	}


	/**
	 * Create a new {@code Random64} instance, where the random numbers are
	 * generated by the given long {@code supplier}.
	 *
	 * @param supplier the random number supplier
	 * @return a new {@code Random64} instance
	 * @throws java.lang.NullPointerException if the given {@code supplier} is
	 *         {@code null}.
	 */
	public static Random64 of(final LongSupplier supplier) {
		Objects.requireNonNull(supplier);

		return new Random64() {
			private static final long serialVersionUID = 1L;

			private final Boolean _sentry = Boolean.TRUE;

			@Override
			public long nextLong() {
				return supplier.getAsLong();
			}

			@Override
			public synchronized void setSeed(final long seed) {
				if (_sentry != null) {
					throw new UnsupportedOperationException(
						"The 'setSeed(long)' method is not supported."
					);
				}
			}
		};
	}

}