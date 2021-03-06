package br.com.wjaa.ranchucrutes.ws.service;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.wjaa.ranchucrutes.commons.form.LoginForm;
import br.com.wjaa.ranchucrutes.commons.utils.StringUtils;
import br.com.wjaa.ranchucrutes.commons.vo.ConfirmaCadastroVo;
import br.com.wjaa.ranchucrutes.commons.vo.PacienteVo;
import br.com.wjaa.ranchucrutes.commons.vo.ProfissionalBasicoVo;
import br.com.wjaa.ranchucrutes.framework.dao.RanchucrutesDao;
import br.com.wjaa.ranchucrutes.ws.adapter.PacienteAdapter;
import br.com.wjaa.ranchucrutes.ws.adapter.ProfissionalAdapter;
import br.com.wjaa.ranchucrutes.ws.dao.LoginDao;
import br.com.wjaa.ranchucrutes.ws.entity.PacienteEntity;
import br.com.wjaa.ranchucrutes.ws.entity.ProfissionalEntity;
import br.com.wjaa.ranchucrutes.ws.entity.RedeSocialEnum;
import br.com.wjaa.ranchucrutes.ws.exception.LoginNotConfirmationException;
import br.com.wjaa.ranchucrutes.ws.exception.LoginServiceException;
import br.com.wjaa.ranchucrutes.ws.exception.LoginSocialException;

/**
 * Created by wagner on 10/08/15.
 */
@Service
@Transactional(readOnly = true, timeout = 10000)
public class LoginServiceImpl implements LoginService {
    private static final Log LOG = LogFactory.getLog(LoginServiceImpl.class);


    @Autowired
    private RanchucrutesDao dao;

    @Autowired
    private LoginDao loginDao;


    @Override
    public String createHashPass(String pass){
        return StringUtils.createMD5(pass);
    }
    

    @Override
    public String createCodeConfirmation(String email, String numeroRegistro){
        LOG.debug("m=createCodeConfirmation, email=" + email + ", numeroRegistro=" + numeroRegistro);
        //criando um md5 com base no email + crm e milisegundo atual.
        return StringUtils.createMD5(email + "|" + numeroRegistro + "|" + new Date().getTime());
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public ConfirmaCadastroVo confirmaCadastro(String code) {
        LOG.debug("m=confirmaCadastro, code=" + code);
        ProfissionalEntity profissionalEntity = dao.getSingleRecordByProperties(ProfissionalEntity.class, "codeConfirmacao", code);
        if (profissionalEntity == null){
            return new ConfirmaCadastroVo(ConfirmaCadastroVo.StatusConfirmacaoCadastro.CODIGO_INVALIDO);
        }

        if (profissionalEntity.getDataConfirmacao() != null){
            return new ConfirmaCadastroVo(ConfirmaCadastroVo.StatusConfirmacaoCadastro.CADASTRO_JA_CONFIRMADO);
        }

        profissionalEntity.setDataConfirmacao(new Date());
        profissionalEntity.setAtivo(true);
        dao.save(profissionalEntity);

        return new ConfirmaCadastroVo(ConfirmaCadastroVo.StatusConfirmacaoCadastro.SUCESSO);
    }
    
    
    
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public ProfissionalEntity confirmeRecuperarSenha(String code) throws LoginServiceException {
        LOG.debug("m=confirmaCadastro, code=" + code);
        ProfissionalEntity profissionalEntity = dao.getSingleRecordByProperties(ProfissionalEntity.class, "codeRecuperacaoSenha", code);
        if (profissionalEntity == null){
            throw new LoginServiceException("Codigo invalido.");
        }
        Calendar c = Calendar.getInstance();
        if (profissionalEntity.getDataConfirmacao() != null && (c.getTime().getDay() - profissionalEntity.getDataRecuperacaoSenha().getDay()) > 1){
            throw new LoginServiceException("Codigo vencido.");
        }
        return profissionalEntity;
    }
    
    
    
    
    
    @Override
    public String createCodeRecovery(String email, String numeroRegistro){
        LOG.debug("m=createCodeRecovery, email=" + email + ", numeroRegistro=" + numeroRegistro);
        //criando um md5 com base no email + crm e milisegundo atual.
        return StringUtils.createMD5(email + "|" + numeroRegistro + "|" + new Date().getTime());
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public ConfirmaCadastroVo recuperaSenha(String code) {
        LOG.debug("m=confirmaCadastro, code=" + code);
        ProfissionalEntity profissionalEntity = dao.getSingleRecordByProperties(ProfissionalEntity.class, "codeConfirmacao", code);
        if (profissionalEntity == null){
            return new ConfirmaCadastroVo(ConfirmaCadastroVo.StatusConfirmacaoCadastro.CODIGO_INVALIDO);
        }

        if (profissionalEntity.getDataConfirmacao() != null){
            return new ConfirmaCadastroVo(ConfirmaCadastroVo.StatusConfirmacaoCadastro.CADASTRO_JA_CONFIRMADO);
        }
        profissionalEntity.setDataConfirmacao(new Date());
        profissionalEntity.setAtivo(true);
        dao.save(profissionalEntity);

        return new ConfirmaCadastroVo(ConfirmaCadastroVo.StatusConfirmacaoCadastro.SUCESSO);
    }
    
    
    
    

    @Override
    public ProfissionalBasicoVo autenticarProfissional(String emailOuCrm, String pass) throws LoginServiceException, LoginNotConfirmationException {
        LOG.debug("m=autenticarProfissional, emailOuCrm=" + emailOuCrm );
        ProfissionalEntity profissionalEntity;
        if (org.apache.commons.lang.StringUtils.isNumeric(emailOuCrm)){
            profissionalEntity = this.loginDao.autenticarProfissional(Integer.valueOf(emailOuCrm), this.createHashPass(pass));
        }else{
            profissionalEntity = this.loginDao.autenticarProfissional(emailOuCrm, this.createHashPass(pass));
        }

        if (profissionalEntity == null){
            throw new LoginServiceException("Login ou senha inválido.");
        }

        if (profissionalEntity.getDataConfirmacao() == null){
            throw new LoginNotConfirmationException("Você não confirmou o seu acesso.");
        }

        if (profissionalEntity.getAtivo() == null || !profissionalEntity.getAtivo()){
            throw new LoginServiceException("Seu acesso está inativado, contate o nosso suporte técnico.");
        }

        return ProfissionalAdapter.toProfissionalBasico(profissionalEntity,null);
    }

    @Override
    public PacienteVo autenticarPaciente(LoginForm form) throws LoginServiceException, LoginSocialException {
        LOG.debug("m=autenticarPaciente, form=" + form );
        if (form.getType() == null){
            throw new LoginServiceException("Rede Social não encontrada!");
        }


        PacienteEntity pacienteEntity;
        if (LoginForm.AuthType.AUTH_RANCHUCRUTES.equals(form.getType())){

            pacienteEntity = this.loginDao.autenticarPaciente(form.getLogin(), this.createHashPass(form.getSenha()));

            if (pacienteEntity == null){
                throw new LoginServiceException("Usuário ou senha inválida");
            }

        }else{
            pacienteEntity = this.loginDao.autenticarPaciente(RedeSocialEnum.adapterSocialType(form.getType()),
                    form.getKeySocial());

            if(pacienteEntity == null){
                throw new LoginSocialException("Usuário não cadastrado!");
            }
        }

        return PacienteAdapter.toPacienteVo(pacienteEntity);
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public PacienteVo registerGcm(Long idLogin, String keyDevice) {
        LOG.debug("m=registerGcm, idLogin=" + idLogin + ", keyDevice=" + keyDevice );
        PacienteEntity pacienteEntity = this.dao.get(PacienteEntity.class,idLogin);

        if (org.apache.commons.lang.StringUtils.isEmpty(pacienteEntity.getKeyDeviceGcm())){
            LOG.info("Adicionando o key device do paciente: " + idLogin);
        }else if (pacienteEntity.getKeyDeviceGcm().equalsIgnoreCase(keyDevice)){
            LOG.info("Substituindo o key device do paciente: " + idLogin + " de: " + pacienteEntity.getKeyDeviceGcm() +
                    " por:" + keyDevice );
        }

        pacienteEntity.setKeyDeviceGcm(keyDevice);
        dao.save(pacienteEntity);

        return PacienteAdapter.toPacienteVo(pacienteEntity);
    }
}
