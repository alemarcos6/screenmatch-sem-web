package br.com.alura.screenmatch.service;

import java.util.List;

public interface IConverteDados {
	
	<T> T obterDados(String josn, Class<T> classe);
	
	<T> List<T> obterLista(String json, Class<T> classe);
	
}
