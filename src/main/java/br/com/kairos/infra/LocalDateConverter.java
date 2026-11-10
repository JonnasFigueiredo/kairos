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

	/** Tela → objeto. Campo vazio vira null; texto que não é data vira erro de campo. */
	@Override
	@SuppressWarnings("rawtypes")
	public Object convertFromString(Map contexto, String[] valores, Class tipo) {
		if (valores == null || valores.length == 0 || valores[0].trim().isEmpty())
			return null;

		try {
			return LocalDate.parse(valores[0].trim());
		} catch (DateTimeParseException e) {
			throw new TypeConversionException("Data inválida: " + valores[0], e);
		}
	}

	/** Objeto → tela. */
	@Override
	@SuppressWarnings("rawtypes")
	public String convertToString(Map contexto, Object valor) {
		return valor == null ? "" : valor.toString();
	}
}
