package com.bitesait.ReportsBot.model;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ToirReestrService {
    @Autowired
    private ToirReestrRepository toirReestrRepository;

    public void deleteReestrFromToir(Reestr reestr) {
        ToirReestr toirReestr = toirReestrRepository.findByReestr(reestr)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reestr ID: " + reestr.getId()));
        toirReestrRepository.delete(toirReestr);
    }
}
