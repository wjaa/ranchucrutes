package br.com.wjaa.ranchucrutes.ws.service;

import br.com.wjaa.ranchucrutes.commons.form.FindProfissionalForm;
import br.com.wjaa.ranchucrutes.commons.form.ProfissionalForm;
import br.com.wjaa.ranchucrutes.commons.vo.DistanceVo;
import br.com.wjaa.ranchucrutes.commons.vo.LocationVo;
import br.com.wjaa.ranchucrutes.commons.vo.ProfissionalBasicoVo;
import br.com.wjaa.ranchucrutes.commons.vo.ResultadoBuscaProfissionalVo;
import br.com.wjaa.ranchucrutes.framework.service.GenericServiceImpl;
import br.com.wjaa.ranchucrutes.framework.service.RanchucrutesService;
import br.com.wjaa.ranchucrutes.ws.adapter.ProfissionalAdapter;
import br.com.wjaa.ranchucrutes.ws.dao.ProfissionalDao;
import br.com.wjaa.ranchucrutes.ws.entity.*;
import br.com.wjaa.ranchucrutes.ws.exception.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by wagner on 12/06/15.
 */
@Service
@Transactional(readOnly = true, timeout = 10000)
public class ProfissionalServiceImpl extends GenericServiceImpl<ProfissionalEntity, Long> implements ProfissionalService {

    private static final int MAX_RAIO = 50;
    private static final Log LOG = LogFactory.getLog(ProfissionalServiceImpl.class);

    @Autowired
    private CepService cepService;

    @Autowired
    private GoogleMapsService googleMapsService;

    private ProfissionalDao profissionalDao;

    @Autowired
    private RanchucrutesService ranchucrutesService;


    @Autowired
    private LoginService loginService;

    @Autowired
    private EmailService emailService;

    @Autowired
    public ProfissionalServiceImpl(ProfissionalDao profissionalDao) {
        super(profissionalDao);
        this.profissionalDao = profissionalDao;
    }

    @Override
    public ResultadoBuscaProfissionalVo find(FindProfissionalForm form) throws CepNotFoundException,
            LocationDuplicateFoundException, LocationNotFoundException {
        LOG.debug("m=find, form=" + form);
        ResultadoBuscaProfissionalVo resultado = new ResultadoBuscaProfissionalVo();

        //pegando o do form caso o usuário usou a sua localizacao
        LocationVo location = form.getLocation();

        //se usuario nao usou sua localizao tentando pegar o cep no cache
        if (location == null){
            location = this.cepService.getLocationByCep(form.getCep());
        }

        //se nao existir no cache pegando o location na api do google
        if (location == null){
            EnderecoEntity endereco = this.cepService.find(form.getCep());
            location = this.googleMapsService.getLatLngByAddress(this.googleMapsService.patternAddress(endereco));
            this.cepService.saveLocation(location,form.getCep());
        }

        resultado.setLatitude(location.getLatitude());
        resultado.setLongitude(location.getLongitude());
        //TODO PENSAR EM UMA SOLUCAO PARA PAGINACAO.
        //TODO CRIAR UMA TABELA DE CONFIGURACAO DO SITEMA, ESSES 50 KM TEM QUE ESTAR NO BANCO.
        //raio de 50 kilometros
        List<ProfissionalEntity> profissionaisProximos = this.profissionalDao.findProfissional(form.getIdEspecialidade(),
                form.getIdCategoria(), location, MAX_RAIO);

        /*
        Se os profissionais encontrados forem menor que 10, então adicionamos na lista mais profissionais que nao aceitam o
        convenio mas aceitam consultas particulares
        */
        if (profissionaisProximos.size() < 10 ){
            profissionaisProximos.addAll(this.profissionalDao.findProfissionalByEspecialidade(form.getIdEspecialidade(),
                    location, MAX_RAIO));
        }

        //TODO DESLIGANDO A BUSCA DE DISTANCIAS
        /*if (profissionaisProximos.size() > 0){
            try{
                //adicionando as distancias em cada endereco.
                this.addDistances(location,profissionaisProximos);
            } catch (DistanceNotFoundException e) {
                LOG.error("Erro ao buscar as distancias para " + form, e);
            }
        }*/
        resultado.setProfissionais(ProfissionalAdapter.toListProfissionalBasico(profissionaisProximos));
        return resultado;
    }

    private void addDistances(LocationVo origin, List<ProfissionalEntity> profissionaisProximos) throws DistanceNotFoundException {
        LocationVo [] locations = {};
        for (ProfissionalEntity m : profissionaisProximos){
            for (ProfissionalClinicaEntity c : m.getClinicas()){
                EnderecoEntity endereco = c.getClinica().getEndereco();
                if (endereco != null && endereco.hasLocation()){
                    locations = (LocationVo[]) ArrayUtils.add(locations, endereco.getLocationVo());
                }
            }
        }

        List<DistanceVo> distanceVoList = this.googleMapsService.getDistance(origin, locations);
        int index = 0;
        for (ProfissionalEntity m : profissionaisProximos){
            for (ProfissionalClinicaEntity c : m.getClinicas()){
                EnderecoEntity endereco = c.getClinica().getEndereco();
                if (endereco != null && endereco.hasLocation()){
                    endereco.setDistanceVo(distanceVoList.get(index++));
                }
            }
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProfissionalEntity saveProfissional(ProfissionalEntity profissional) throws ProfissionalServiceException {
        this.validate(profissional);
        LOG.debug("m=saveProfissional, profissional=" + profissional);
        LOG.info("Novo profissional, criando....");
        return this.insertProfissional(profissional);
    }

    
    
	@Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
	public ProfissionalEntity recuperarSenha(ProfissionalEntity profissional) throws ProfissionalServiceException {
		if(profissional.getEmail() == null) {
            throw new ProfissionalServiceException("Impossivel reucuperar o email");
		}
		LOG.debug("m=recuperarSenha, profissional=" + profissional.getEmail());
		ProfissionalEntity profissionalEntity = this.profissionalDao.getProfissionalByEmail(profissional.getEmail());
		if(profissionalEntity == null) {
            throw new ProfissionalServiceException("Profissional não encontrado");
		}else if(profissionalEntity.getDataConfirmacao() == null) {
            throw new ProfissionalServiceException("Profissional ainda não foi  confirmado. Verifique o email");
		}else if(!profissionalEntity.getAtivo()) {
            throw new ProfissionalServiceException("Profissional inativo");

		}else {
			profissionalEntity.setCodeRecuperacaoSenha(loginService.createCodeRecovery(profissional.getEmail(), profissional.getNumeroRegistro()));
			profissionalEntity.setDataRecuperacaoSenha(new Date());
			update(profissionalEntity);
			emailService.sendEmailRecuperarSenhaProfissional(profissionalEntity.getEmail(), profissionalEntity.getNome(), profissionalEntity.getCodeRecuperacaoSenha());
		}	
			
			return profissionalEntity;
	}
    
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
	public ProfissionalEntity alterarSenha(ProfissionalEntity profissional) throws ProfissionalServiceException {
		LOG.debug("m=alterarSenha, profissional=" + profissional);
			ProfissionalEntity profissionalEntity = this.profissionalDao.get(profissional.getIdLogin());
			profissionalEntity.setSenha(loginService.createHashPass(profissional.getSenha()));
			profissionalEntity.setDataRecuperacaoSenha(null);
			profissionalEntity.setCodeRecuperacaoSenha("");
			update(profissionalEntity);
			return profissional;

	}

	@Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProfissionalEntity update(ProfissionalEntity profissional) throws ProfissionalServiceException {
		LOG.debug("m=update, profissional=" + profissional);
        if (profissional.getIdLogin() == null){
            throw new ProfissionalServiceException("Impossivel atualizar um profissional sem ID");
        }
        ProfissionalEntity profissionalExists = this.profissionalDao.get(profissional.getIdLogin());
        LOG.info("Atualizando profissional [" + profissional.getIdLogin() + "]");
    	System.out.println(profissional.getSenha());
    	System.out.println(profissionalExists.getSenha());

        if(!profissionalExists.getSenha().toString().equals(profissional.getSenha().toString())) {
        profissional.setSenha(loginService.createHashPass(profissional.getSenha()));
        }
        
       	System.out.println(profissional.getSenha());
    	System.out.println(profissionalExists.getSenha());
        this.saveClinicas(profissionalExists, profissional.getClinicas());
        return this.mergeProfissional(profissionalExists, profissional);
    }

    @Override
    public ProfissionalBasicoVo getProfissionalBasico(Long idProfissional) {
        return ProfissionalAdapter.toProfissionalBasico(get(idProfissional),null);
    }

    @Override
    public boolean profissionalAceitaCategoria(Long idProfissional, Long idClinica,  Integer ... idsCategoria) throws ProfissionalServiceException {
        LOG.debug("m=profissionalAceitaCategoria, idProfissional=" + idProfissional +
                ", idClinica=" + idClinica + ", idsCategoria=" + idsCategoria);
        if (idsCategoria == null || idsCategoria.length == 0){
            throw new ProfissionalServiceException("Paciente não configurou plano de saúde.");
        }

        ProfissionalEntity profissionalEntity = this.profissionalDao.getProfissionalByIdAndCategoria(idProfissional,idClinica,idsCategoria);
        return profissionalEntity != null;
    }

    @Override
    public ProfissionalOrigemEntity getParceiro(Long idProfissional, Long idClinica) {
        LOG.debug("m=getParceiro, idProfissional=" + idProfissional +
                ", idClinica=" + idClinica );
        return profissionalDao.findProfissionalOrigem(idProfissional,idClinica);
    }

    @Override
    public List<ProfissionalBasicoVo> findByStartName(String startName) {
        LOG.debug("m=findByStartName, startName=" + startName);
        if (StringUtils.isEmpty(startName)){
            return null;
        }
        if (startName.length() < 5){
            return null;
        }

        List<ProfissionalEntity> profissionais = profissionalDao.findProfissionalByStartName(startName);
        return ProfissionalAdapter.toListProfissionalBasico(profissionais);
    }

    private ProfissionalEntity mergeProfissional(ProfissionalEntity profissionalPersisted, ProfissionalEntity profissional) {
        LOG.debug("m=mergeProfissional, profissionalPersisted=" + profissionalPersisted + ", profissional" + profissional);
        BeanUtils.copyProperties(profissional,profissionalPersisted,"idLogin",
                "dataUltimoAcesso",
                "codeConfirmacao",
                "dataCriacao",
                "dataConfirmacao","ativo");

        return profissionalDao.save(profissionalPersisted);
    }

    private ProfissionalEntity insertProfissional(ProfissionalEntity profissional) throws ProfissionalServiceException {
        LOG.debug("m=insertProfissional, profissional" + profissional);
        profissional.setDataCriacao(new Date());
        profissional.setDataUltimoAcesso(new Date());
        profissional.setAtivo(false);
        profissional.setAtendente(false);
        try {
            profissional.setCodeConfirmacao(loginService.createCodeConfirmation(profissional.getEmail(),profissional.getNumeroRegistro()));
            profissional.setSenha(loginService.createHashPass(profissional.getSenha()));
        } catch (Exception e) {
            throw new ProfissionalServiceException("Erro ao gerar senha|codigo de confirmacao do cliente");
        }
        profissional = profissionalDao.save(profissional);
        LOG.info("Enviando email de confirmacao para " + profissional.getEmail());
        emailService.sendEmailNovoProfissional(profissional.getEmail(), profissional.getNome(), profissional.getCodeConfirmacao());
        return profissional;

    }

    @Override
    public void saveClinicas(ProfissionalEntity profissionalPersisted, List<ProfissionalClinicaEntity> clinicas)
            throws ProfissionalServiceException {
        LOG.debug("m=saveClinicas, profissionalPersisted" + profissionalPersisted);
        for (ProfissionalClinicaEntity profissionalClinica : clinicas){
            ClinicaEntity clinica = profissionalClinica.getClinica();
            EnderecoEntity endereco = clinica.getEndereco();
            endereco = this.saveEndereco(endereco);
            AgendaEntity agenda = clinica.getAgenda();
            clinica.setEndereco(endereco);
            clinica = this.ranchucrutesService.saveWithRequied(clinica);

            profissionalClinica.setClinica(clinica);
            profissionalClinica.setIdProfissional(profissionalPersisted.getIdLogin());
            this.ranchucrutesService.saveWithRequied(profissionalClinica);
            //salvando a agenda.
            if (agenda != null){
                List<AgendaHorarioEntity> agendaHorarios = agenda.getAgendaHorarios();
                agenda.setIdClinica(clinica.getId());
                this.ranchucrutesService.saveWithRequied(agenda);
                //this.saveAgendaHorarios(agenda.getId(),agendaHorarios);

            }
        }
        //verificando se alguma clinica foi exluida.
        for (ProfissionalClinicaEntity clinicaPersisted: profissionalPersisted.getClinicas()){
            if ( !clinicas.contains(clinicaPersisted)){
                if ( clinicaPersisted.getClinica() != null ){
                    this.removeClinica(clinicaPersisted.getId());
                }
            }
        }

    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAgendaHorarios(List<ProfissionalClinicaEntity> clinicas){
        LOG.debug("m=saveAgendaHorarios");
        for (ProfissionalClinicaEntity profissionalClinica: clinicas){
            AgendaEntity agenda = profissionalClinica.getClinica().getAgenda();
            if (agenda != null){
                this.saveAgendaHorarios(agenda.getId(),agenda.getAgendaHorarios());
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeClinica(Long idProfissionalClinica) throws ProfissionalServiceException {
        LOG.debug("m=removeClinica, idProfissionalClinica=" + idProfissionalClinica);
        this.ranchucrutesService.removeByProperties(ProfissionalClinicaEntity.class, "id", idProfissionalClinica);
    }

    private EnderecoEntity saveEndereco(EnderecoEntity endereco) throws ProfissionalServiceException {
        LOG.debug("m=saveEndereco, endereco=" + endereco);
        try {
            //validando os dados do cep
            this.validateEndereco(endereco);

            LocationVo location = cepService.getLocationByCep(endereco.getCep());
            if (location == null){
                LOG.debug("Cep nao encontrado no cache, busca latitude e longitude no google");
                location = this.googleMapsService.getLatLngByAddress(this.googleMapsService.patternAddress(endereco));
                cepService.saveLocation(location,endereco.getCep());
            }
            endereco.setLatitude(location.getLatitude());
            endereco.setLongitude(location.getLongitude());
        } catch (LocationNotFoundException | LocationDuplicateFoundException e) {
            LOG.error("Erro ao buscar a latitude/longitude do endereco do profissional", e);
        }

        endereco = this.ranchucrutesService.saveWithRequied(endereco);
        return endereco;
    }

    private void validateEndereco(EnderecoEntity endereco) throws ProfissionalServiceException {
        if (StringUtils.isBlank(endereco.getCep())){
            throw new ProfissionalServiceException("Cep do endereço não pode ser nulo.");
        }
    }


    /**
     *
     * @param profissional
     * @throws ProfissionalServiceException
     */
    private void validate(ProfissionalEntity profissional) throws ProfissionalServiceException {
        LOG.debug("m=validate, profissional=" + profissional);
        //TODO VERIFICAR ESSA CONSISTENCIA PQ AGORA TEREMOS VARIOS TIPO DE REGISTRO
        /*if (profissional.getNumeroRegistro() == null){
            LOG.error("Profissional sem numero de registro...");
            throw new ProfissionalServiceException("CRM não pode ser null");
        }*/
        ProfissionalEntity profissionalExists = null;

        if ( StringUtils.isNotBlank(profissional.getNumeroRegistro()) ){
            profissionalExists = this.profissionalDao.getProfissionalByNumeroRegistro(profissional.getNumeroRegistro());
            if (profissionalExists != null){
                throw new ProfissionalServiceException("Já existe um profissional cadastrado com esse CRM [" + profissional.getNumeroRegistro() + "]");
            }
        }

        /*if ( StringUtils.isNotBlank(profissional.getEmail()) ){
            profissionalExists = this.profissionalDao.getProfissionalByEmail(profissional.getEmail());
            if (profissionalExists != null){
                throw new ProfissionalServiceException("Já existe um profissional cadastrado com esse Email [" + profissional.getEmail() + "]");
            }
        }*/

    }

    private void saveAgendaHorarios(Long idAgenda, List<AgendaHorarioEntity> agendaHorarios) {
        LOG.debug("m=saveAgendaHorarios, idAgenda=" + idAgenda);
        AgendaEntity agenda = this.ranchucrutesService.get(AgendaEntity.class,idAgenda);
        if (!CollectionUtils.isEmpty(agendaHorarios)){
            for(AgendaHorarioEntity ah : agendaHorarios){
                ah.setIdAgenda(idAgenda);
                this.ranchucrutesService.saveWithRequied(ah);
            }

            if (agenda.getAgendaHorarios() != null){
                //verificando se algum horario foi excluido
                for (AgendaHorarioEntity ahe: agenda.getAgendaHorarios()){
                    if ( !agendaHorarios.contains(ahe)){
                        this.ranchucrutesService.removeByProperties(AgendaHorarioEntity.class,"id",ahe.getId());
                    }
                }
            }
        }


    }



}
