package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;
  // Make a map for checking if the identifier is a reserved word or not
  private static final Map<String, TokenType> keywords;
  
  static {
    keywords.put("and", AND);
    keywords.put("class", CLASS);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("for", FOR);
    keywords.put("fun", FUN);
    keywords.put("if", IF);
    keywords.put("nil", NIL);
    keywords.put("or", OR);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("this", THIS);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }
  List<Token> scanTokens() {
    while(!isAtEnd()) {
      start = current;
      scanToken()
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  // Below tries to scan for single character lexemes based on token type

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      
      // include non single character operators while checking for its independent value
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<': 
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/': 
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) advance(); // identifies a comment and while the conditions are true skips thru it
        } else {
          addToken(SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        // To ignore white space just break to go back to the beginning of the scan loop
        break;
      case '\n':
        line++;
        break;
      case '"': string(); break;
      default:
        // Tedious to look for each case of the decimal number instead just default it
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        }else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type); 
  }
  
  // Check for digit
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
  
  private void number() {
    while (isDigit(peek())) advance();
    // Peek checks if we're at the end of source code. If not, check if decimal.
    if (peek() == '.' && isDigit(peekNext())) {
      // start is on '.' need to move past it:
      advance();
      while (isDigit(peek())) advance();
    }
    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }
  
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line ++;
      advance();
    }
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }
    // We've reached the closing " thus breaking out of the while peek() != '"'. Need to push past it.
    advance();
    // Trim the quotes off.
    String value = source.substring(start + 1, current - 1); // Remember start doesn't reset value until scanTokens' while loop. start and current are both '"'.
    addToken(STRING, value);
  }
  
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1); //current++ would increment prev value by 1 but would still be expressed as the prev value. 
  }
  
  // Check for alphabet
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }
  // If and only if we're dealing with identifiers do we then check if alphanumeric
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c)
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  // peek 'looks ahead' and checks if we're at the end of the program 
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  
  private boolean isAtEnd() {
    return current >= source.length()
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}

