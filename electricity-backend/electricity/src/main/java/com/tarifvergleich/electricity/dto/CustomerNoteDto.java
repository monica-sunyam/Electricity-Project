package com.tarifvergleich.electricity.dto;

import java.math.BigInteger;

import com.tarifvergleich.electricity.model.CustomerNote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CustomerNoteDto {
	private Integer noteId;
	private String note;
	private BigInteger addedOn;
	private Integer customerId;
	private Integer adminId;

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Data
	public static class CustomerNoteResponseDto {
		private Integer noteId;
		private String note;
		private BigInteger addedOn;
	}
	
	public static CustomerNoteResponseDto mapNoteResponse(CustomerNote note) {
		if(note == null) return null;
		
		return CustomerNoteResponseDto.builder()
				.noteId(note.getId())
				.note(note.getNote())
				.addedOn(note.getAddedOn())
				.build();
	}
}
