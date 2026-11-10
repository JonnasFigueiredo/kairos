package br.com.kairos.infra;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

import com.opensymphony.xwork2.conversion.TypeConversionException;

/**
 * Ensina o Struts a converter o texto de um campo de data em LocalDate, e de volta.
 *
 * O campo <input type="date"> envia e recebe sempre no formato AAAA-MM-DD,
 * que é o padrão de LocalDate.parse e de LocalDate.toString.
 * Registrado em xwork-conversion.properties.
 */
public class LocalDateConverter extends StrutsTypeConverter {

	/** Faixa de anos que a coluna DATE do MySQL aceita. */
	private static final int ANO_MINIMO = 1000;
	private static final int ANO_MAXIMO = 9999;

	/** Tela → objeto. Campo vazio vira null; texto que não é data vira erro de campo. */
	@Override
	@SuppressWarnings("rawtypes")
	public Object convertFromString(Map contexto, String[] valores, Class tipo) {
		if (valores == null || valores.length == 0 || valores[0].trim().isEmpty())
			return null;

		LocalDate data;
		try {
			data = LocalDate.parse(valores[0].trim());
		} catch (DateTimeParseException e) {
			throw new TypeConversionException("Data inválida: " + valores[0], e);
		}

		if (data.getYear() < ANO_MINIMO || data.getYear() > ANO_MAXIMO)
			throw new TypeConversionException("Ano fora da faixa aceita: " + valores[0]);

		return data;
	}

	/** Objeto → tela. */
	@Override
	@SuppressWarnings("rawtypes")
	public String convertToString(Map contexto, Object valor) {
		return valor == null ? "" : valor.toString();
	}
}
