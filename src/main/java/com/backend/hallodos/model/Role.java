package com.backend.hallodos.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role {

    @Id
    private long id;


    @OneToOne
    @JoinColumn(name = "dosen_id")
    private Dosen dosen;

}
