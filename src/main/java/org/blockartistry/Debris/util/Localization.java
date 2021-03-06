/*
 * This file is part of Debris, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.Debris.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;

public final class Localization {

	private static Local impl;

	private abstract static class Local {
		@Nonnull
		public abstract String format(@Nonnull final String translateKey, @Nullable final Object... parameters);
	}

	private static class ClientImpl extends Local {
		public ClientImpl() {
		}

		@Nonnull
		public String format(@Nonnull final String translateKey, @Nullable final Object... parameters) {
			// Let I18n do the heavy lifting
			return I18n.format(translateKey, parameters);
		}
	}

	// Manually loads the en_US language file. Not looking for translations -
	// just want to reuse the strings in the language file.
	private static class ServerImpl extends Local {

		private final Translations xlate = new Translations();

		public ServerImpl() {
			this.xlate.load("/assets/debris/lang/", Translations.DEFAULT_LANGUAGE);
		}

		@Override
		@Nonnull
		public String format(@Nonnull final String translateKey, @Nullable final Object... parameters) {
			return this.xlate.format(translateKey, parameters);
		}
	}

	public static void initialize(@Nonnull final Side side) {
		if (side == Side.SERVER) {
			impl = new ServerImpl();
		} else {
			impl = new ClientImpl();
		}
	}

	@Nonnull
	public static String format(@Nonnull final String translateKey, @Nullable final Object... parameters) {
		return impl.format(translateKey, parameters);
	}
}
