package it.multicoredev.mclib.console;

import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright © 2019-2023 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public enum Colors {

    BLACK("black", "0", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString()),
    DARK_BLUE("dark_blue", "1", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString()),
    DARK_GREEN("dark_green", "2", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString()),
    DARK_AQUA("dark_aqua", "3", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString()),
    DARK_RED("dark_red",  "4",Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString()),
    DARK_PURPLE("dark_purple", "5", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString()),
    GOLD("gold", "6", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString()),
    GRAY("gray", "7", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString()),
    DARK_GRAY("dark_gray", "8", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString()),
    BLUE("blue", "9", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString()),
    GREEN("green", "a", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString()),
    AQUA("aqua", "b", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString()),
    RED("red", "c", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString()),
    LIGHT_PURPLE("light_purple", "d", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString()),
    YELLOW("yellow", "e", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString()),
    WHITE("white", "f", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString()),

    MAGIC("magic", "k", Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString()),
    BOLD("bold", "m", Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString()),
    STRIKETHROUGH("strikethrough", "l", Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString()),
    UNDERLINE("underline", "n", Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString()),
    ITALIC("italic", "o", Ansi.ansi().a(Ansi.Attribute.ITALIC).toString()),
    RESET("reset", "r", Ansi.ansi().a(Ansi.Attribute.RESET).toString());

    private String name;
    private String colorCode;
    private String color;

    Colors(String name, String colorCode, String color){
        this.name = name;
        this.colorCode = "&" + colorCode;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString(){
        return color;
    }

    public static String getByName(@NotNull String name){
        for(Colors color : Colors.values()){
            if(color.getName().equals(name)){
                return color.getColor();
            }
        }
        return "";
    }

    public static String translateColorCodes(String text){
        if (text == null || text.trim().isEmpty()) return text;

        for(Colors color : Colors.values()){
            text = text.replace(color.getColorCode(), color.getColor());
        }

        return text;
    }
}
