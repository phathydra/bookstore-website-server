package com.bookstore.accounts.mapper;

import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.entity.Information;

public class InformationMapper {
    public static InformationDto mapToInformationDto(Information information, InformationDto informationDto){
        informationDto.setId(information.getId());
        informationDto.setAccountId(information.getAccountId());
        informationDto.setName(information.getName());
        informationDto.setEmail(information.getEmail());
        informationDto.setAddress(information.getAddress());
        informationDto.setPhone(information.getPhone());
        informationDto.setAvatar(information.getAvatar());
        return informationDto;
    }

    public static Information mapToInformation(InformationDto informationDto, Information information){
        information.setAccountId(informationDto.getAccountId());
        information.setName(informationDto.getName());
        information.setEmail(informationDto.getEmail());
        information.setAddress(informationDto.getAddress());
        information.setPhone(informationDto.getPhone());
        information.setAvatar(informationDto.getAvatar());
        return information;
    }
}
