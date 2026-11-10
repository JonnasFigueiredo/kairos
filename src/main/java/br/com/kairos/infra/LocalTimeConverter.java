package br.com.kairos.infra;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

import com.opensymphony.xwork2.conversion.TypeConversionException;

/**
 * Ensina o Struts a converter o texto de um campo de hora em LocalTime, e de volta.
 *
 * O campo <input type="time"> envia e recebe no formato HH:mm.
 * Registrado em xwork-conversion.properties.
 */
public class LocalTimeConverter extends StrutsTypeConverter {

	private static final DateTimeFormatter HORA_E_MINUTO = DateTimeFormatter.ofPattern("HH:mm");

	/** Tela → objeto. Campo vazio vira null; texto que não é hora vira erro de campo. */
	@Override
	@SuppressWarnings("rawtypes")
	public Object convertFromString(Map contexto, String[] valores, Class tipo) {
		if (valores == null || valores.length == 0 || valores[0].trim().isEmpty())
			return null;

		try {
			return LocalTime.parse(valores[0].trim());
		} catch (DateTimeParseException e) {
			throw new TypeConversionException("Horário inválido: " + valores[0], e);
		}
	}

	/** Objeto → tela. */
	@Override
	@SuppressWarnings("rawtypes")
	public String convertToString(Map contexto, Object valor) {
		return valor == null ? "" : ((LocalTime) valor).format(HORA_E_MINUTO);
	}
}
