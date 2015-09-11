package br.com.wjaa.ranchucrutes.ws.dao;

import br.com.wjaa.ranchucrutes.ws.entity.LoginEntity;
import br.com.wjaa.ranchucrutes.ws.entity.MedicoEntity;
import br.com.wjaa.ranchucrutes.ws.entity.PacienteEntity;
import br.com.wjaa.ranchucrutes.ws.entity.RedeSocialEnum;

import java.util.List;

/**
 * Created by wagner on 18/08/15.
 */
public interface LoginDao extends GenericDao<LoginEntity,Integer>{

    MedicoEntity autenticarMedico(String email, String senha);

    MedicoEntity autenticarMedico(Integer crm, String senha);

    PacienteEntity autenticarPaciente(String login, String senha);

    PacienteEntity autenticarPaciente(RedeSocialEnum redeSocial, String keySocial);
}
