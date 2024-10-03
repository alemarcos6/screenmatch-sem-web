package br.com.alura.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {

    private Scanner leitura = new Scanner(System.in); 
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    public void exibeMenu(){
		System.out.println("Digite o nome da série para a busca");

		var nomeSerie = leitura.nextLine();
		var enderecoCompleto = ENDERECO + nomeSerie.replace(" ", "+") + API_KEY;
		var json = consumo.obterDados(enderecoCompleto);

		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

    	System.out.println(dados);
        
    	List<DadosTemporada> temporadas = new ArrayList<>();
   
    	for(int i = 1; i<=dados.totalTemporadas(); i++) {
    		json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") +"&season=" + i + API_KEY);
    		DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
    		temporadas.add(dadosTemporada);
    	}
    	
//        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
        
    	System.out.println("\n Top 5 episódios");

    	temporadas.stream()
    		.flatMap(t -> t.episodios().stream())
    		.filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
    		.sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
        	.limit(5)
        	.forEach(System.out::println);
    	
    	List<Episodio> episodios = temporadas.stream()
    			.flatMap(t-> t.episodios().stream()
    					.map(d -> new Episodio(t.numero(), d))
    			).toList();
    	
    	episodios.forEach(System.out::println);
    	
//    	System.out.println("Digite um trecho do título do episódio:");
//    	var trechoTitulo = leitura.nextLine();
//    	
//    	Optional<Episodio> episodioBuscado = episodios.stream()
//    			.filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//    			.findFirst();
//    	
//    	if (episodioBuscado.isPresent()) {
//			System.out.println("Episódio encontrado!");
//			System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//			System.out.println("Título: " + episodioBuscado.get().getTitulo());
//		} else {
//			System.out.println("Episódio não encontrado!");
//		}
    	
//    	System.out.println("A partir de que ano você deseja ver os episódios? ");
//    	var ano = leitura.nextInt();
//    	leitura.nextLine();
//    	
//    	LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//    	DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//    	
//    	episodios.stream()
//    		.filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//    		.forEach(e -> System.out.println(
//    				"Temporada: " + e.getTemporada() +
//    				" Episódio: " + e.getTitulo() +
//    				" Data lançamento: " + e.getDataLancamento().format(formatador)
//    		));
    	
    	Map<Integer,Double> avaliacoesPorTemporada = episodios.stream().filter(e -> e.getAvaliacao() > 0.0).collect(
    				Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao))
    			);
    	
    	avaliacoesPorTemporada.entrySet().stream().sorted(Map.Entry.<Integer,Double>comparingByValue().reversed()).forEach(System.out::println);
    	
    	DoubleSummaryStatistics estatistica = episodios.stream().filter(e -> e.getAvaliacao() > 0.0).collect(
    			Collectors.summarizingDouble(Episodio::getAvaliacao));
    	
    	System.out.println("Média: " + estatistica.getAverage());
    	System.out.println("Melhor episódio: " + estatistica.getMax());
    	System.out.println("Pior episódio: " + estatistica.getMin());
    	System.out.println("Quantidade: " + estatistica.getCount());
    	
    }
    	
}