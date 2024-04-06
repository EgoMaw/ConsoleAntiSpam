package io.github.egomaw.consoleantispam;

import org.spongepowered.configurate.serialize.SerializationException;

import java.io.PrintStream;

public final class SystemPrintFilter extends PrintStream {
	public SystemPrintFilter(PrintStream stream) {
		super(stream);
	}

	@Override
	public void println(String x) {
		try {
			if (!Main.shouldFilterMessage(x)) super.println(x);
		} catch (SerializationException e) {
			super.println(x);
		}
	}

	@Override
	public void print(String s) {
		try {
			if (!Main.shouldFilterMessage(s)) super.print(s);
		} catch (SerializationException e) {
			super.print(s);
		}
	}
}
