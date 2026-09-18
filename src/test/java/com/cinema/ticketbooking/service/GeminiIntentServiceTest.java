package com.cinema.ticketbooking.service;
import com.cinema.ticketbooking.domain.request.ReqAssistantChatDto;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GeminiIntentServiceTest {
 ObjectMapper mapper=new ObjectMapper().findAndRegisterModules();
 GeminiIntentService service=new GeminiIntentService(mapper,Validation.buildDefaultValidatorFactory().getValidator(),"","");
 String response(String text,String finish) throws Exception {return mapper.writeValueAsString(java.util.Map.of("candidates",java.util.List.of(java.util.Map.of("finishReason",finish,"content",java.util.Map.of("parts",java.util.List.of(java.util.Map.of("text",text)))))));}
 @Test void parsesStructuredOutputAndValidatesBounds() throws Exception {
  var json="{\"intent\":\"MOVIES\",\"genre\":\"Hài\",\"maxDuration\":120,\"date\":\"2026-09-20\",\"people\":null,\"budget\":null}";
  var parsed=service.parseResponse(response(json,"STOP"));assertEquals(120,parsed.maxDuration());assertEquals("2026-09-20",parsed.date().toString());
  var invalid=response(json.replace("120","601"),"STOP");assertThrows(BadRequestException.class,()->service.parseResponse(invalid));
 }
 @Test void rejectsBlockedAndTruncatedOutput() throws Exception {var truncated=response("{}","MAX_TOKENS");assertThrows(BadRequestException.class,()->service.parseResponse(truncated));assertThrows(BadRequestException.class,()->service.parseResponse("{}"));}
 @Test void missingConfigurationDoesNotCallProvider(){assertThrows(BadRequestException.class,()->service.interpret(new ReqAssistantChatDto()));}
}
