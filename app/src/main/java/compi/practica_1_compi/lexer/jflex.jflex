package compi.practica_1_compi.lexer;

import java_cup.runtime.Symbol;
import java.util.ArrayList;
import java.util.List;
import compi.practica_1_compi.parser.sym;

%%

%public
%unicode
%class Lexer
%cup
%line
%column

%{

public List<String> errorList = new ArrayList<>();
public List<Symbol> tokenList = new ArrayList<>();

public List<String> getLexicalErrors() {
    return errorList;
}

public List<Symbol> getTokens() {
    return tokenList;
}

private Symbol symbol(int type) {
    int start = (int) yychar;
    int end = start + yytext().length();
    Symbol s = new Symbol(type, start, end, yytext());
    tokenList.add(s);
    return s;
}

private Symbol symbol(int type, Object value) {
    int start = (int) yychar;
    int end = start + yytext().length();
    Symbol s = new Symbol(type, start, end, value);
    tokenList.add(s);
    return s;
}

%}

/* ----------- MACROS ----------- */

DIGITO   = [0-9]
LETRA    = [a-zA-Z]
ID       = {LETRA}({LETRA}|{DIGITO})*
ENTERO   = {DIGITO}+
DECIMAL  = {DIGITO}+"."{DIGITO}+
ESPACIO  = [ \t\r\n]+
CADENA   = \"([^\"\n])*\"


%%

"INICIO"                { return symbol(sym.INICIO); }
"FIN"                   { return symbol(sym.FIN); }
"VAR"                   { return symbol(sym.VAR); }
"SI"                    { return symbol(sym.SI); }
"ENTONCES"              { return symbol(sym.ENTONCES); }
"FINSI"                 { return symbol(sym.FINSI); }
"MIENTRAS"              { return symbol(sym.MIENTRAS); }
"HACER"                 { return symbol(sym.HACER); }
"FINMIENTRAS"           { return symbol(sym.FINMIENTRAS); }
"MOSTRAR"               { return symbol(sym.MOSTRAR); }
"LEER"                  { return symbol(sym.LEER); }

"+"                     { return symbol(sym.MAS); }
"-"                     { return symbol(sym.MENOS); }
"*"                     { return symbol(sym.POR); }
"/"                     { return symbol(sym.DIV); }

"=="                    { return symbol(sym.IGUAL); }
"!="                    { return symbol(sym.DIFERENTE); }
"<>"                    { return symbol(sym.DIFERENTE); }
">="                    { return symbol(sym.MAYOR_IGUAL); }
"<="                    { return symbol(sym.MENOR_IGUAL); }
">"                     { return symbol(sym.MAYOR); }
"<"                     { return symbol(sym.MENOR); }

"&&"                    { return symbol(sym.AND); }
"||"                    { return symbol(sym.OR); }
"!"                     { return symbol(sym.NOT); }

"="                     { return symbol(sym.ASIGNACION); }
"("                     { return symbol(sym.PAREN_IZQ); }
")"                     { return symbol(sym.PAREN_DER); }
"%%%%"                  { return symbol(sym.SEPARADOR); }
"|"                     { return symbol(sym.BARRA); }
","                     { return symbol(sym.COMA); }

"%DEFAULT"              { return symbol(sym.DEFAULT); }
"%COLOR_TEXTO_SI"       { return symbol(sym.COLOR_TEXTO_SI); }
"%COLOR_SI"             { return symbol(sym.COLOR_SI); }
"%FIGURA_SI"            { return symbol(sym.FIGURA_SI); }
"%LETRA_SI"             { return symbol(sym.LETRA_SI); }
"%LETRA_SIZE_SI"        { return symbol(sym.LETRA_SIZE_SI); }

"%COLOR_TEXTO_MIENTRAS" { return symbol(sym.COLOR_TEXTO_MIENTRAS); }
"%COLOR_MIENTRAS"       { return symbol(sym.COLOR_MIENTRAS); }
"%FIGURA_MIENTRAS"      { return symbol(sym.FIGURA_MIENTRAS); }
"%LETRA_MIENTRAS"       { return symbol(sym.LETRA_MIENTRAS); }
"%LETRA_SIZE_MIENTRAS"  { return symbol(sym.LETRA_SIZE_MIENTRAS); }

"%COLOR_TEXTO_BLOQUE"   { return symbol(sym.COLOR_TEXTO_BLOQUE); }
"%COLOR_BLOQUE"         { return symbol(sym.COLOR_BLOQUE); }
"%FIGURA_BLOQUE"        { return symbol(sym.FIGURA_BLOQUE); }
"%LETRA_BLOQUE"         { return symbol(sym.LETRA_BLOQUE); }
"%LETRA_SIZE_BLOQUE"    { return symbol(sym.LETRA_SIZE_BLOQUE); }

"ELIPSE"                { return symbol(sym.ELIPSE); }
"CIRCULO"               { return symbol(sym.CIRCULO); }
"PARALELOGRAMO"         { return symbol(sym.PARALELOGRAMO); }
"RECTANGULO"            { return symbol(sym.RECTANGULO); }
"ROMBO"                 { return symbol(sym.ROMBO); }
"RECTANGULO_REDONDEADO" { return symbol(sym.RECTANGULO_REDONDEADO); }

"ARIAL"                 { return symbol(sym.ARIAL); }
"TIMES_NEW_ROMAN"       { return symbol(sym.TIMES_NEW_ROMAN); }
"COMIC_SANS"            { return symbol(sym.COMIC_SANS); }
"VERDANA"               { return symbol(sym.VERDANA); }

{DECIMAL}               { return symbol(sym.DECIMAL, yytext()); }
{ENTERO}                { return symbol(sym.ENTERO, yytext()); }
{CADENA}                { return symbol(sym.CADENA, yytext()); }
{ID}                    { return symbol(sym.ID, yytext()); }

"#".*                   { /* comentario */ }
{ESPACIO}               { /* ignora espacios */ }

/* ----------- ERROR LÉXICO ----------- */

. {
    errorList.add(
        "Léxico -> Lexema: '" + yytext() +
        "' Línea: " + (yyline + 1) +
        " Columna: " + (yycolumn + 1)
    );
}
