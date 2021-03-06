package br.com.wjaa.ranchucrutes.ws.controller;

import br.com.wjaa.ranchucrutes.commons.vo.ConvenioCategoriaVo;
import br.com.wjaa.ranchucrutes.framework.service.RanchucrutesService;
import br.com.wjaa.ranchucrutes.ws.adapter.RanchucrutesAdapter;
import br.com.wjaa.ranchucrutes.ws.entity.*;
import br.com.wjaa.ranchucrutes.ws.exception.CepNotFoundException;
import br.com.wjaa.ranchucrutes.ws.service.CepService;
import br.com.wjaa.ranchucrutes.commons.vo.EnderecoVo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by wagner on 18/06/15.
 */
@RestController
public class RanchucrutesWS extends BaseWS {

    private static final Log LOG = LogFactory.getLog(RanchucrutesWS.class);


    @Autowired
    private RanchucrutesService ranchucrutesService;

    @Autowired
    private CepService cepService;

    @RequestMapping(value = "/convenio/all", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    List<ConvenioEntity> listAllConvenio() {
        List<ConvenioEntity> result = this.ranchucrutesService.getByProperties(ConvenioEntity.class,"ativo",true);
        return result;
    }

    @RequestMapping(value = "/convenio/categoria/all", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    List<ConvenioCategoriaVo> listAllConvenioCategorias() {
        List<ConvenioCategoriaEntity> categorias = this.ranchucrutesService.getByProperties(ConvenioCategoriaEntity.class,"ativo",true);
        return RanchucrutesAdapter.toConvenioCategoriaVo(categorias);
    }


    @RequestMapping(value = "/convenio/categoria/{idConvenio}", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    List<ConvenioCategoriaVo> getCategoriasByIdConvenio(@PathVariable Integer idConvenio) {
        List<ConvenioCategoriaEntity> categorias = this.ranchucrutesService.getByProperties(ConvenioCategoriaEntity.class,
                new String[]{"idConvenio","ativo"}, new Object[]{idConvenio,true});
        return RanchucrutesAdapter.toConvenioCategoriaVo(categorias);
    }

    @RequestMapping(value = "/categoria/{idCategoria}", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    ConvenioCategoriaVo getCategoriaById(@PathVariable Integer idCategoria) {
        ConvenioCategoriaEntity categoria = this.ranchucrutesService.get(ConvenioCategoriaEntity.class,
                idCategoria);
        return RanchucrutesAdapter.toConvenioCategoriaVo(categoria);
    }

    @RequestMapping(value = "/especialidade/all", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    List<EspecialidadeEntity> listAllEspecialidade() {
        return this.ranchucrutesService.getByProperties(EspecialidadeEntity.class,"ativo",true);
    }

    @RequestMapping(value = "/especialidade/{idProfissao}", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    List<EspecialidadeEntity> listEspecialidadeByProfissao(@PathVariable Integer idProfissao) {
        return this.ranchucrutesService.getByProperties(EspecialidadeEntity.class,"profissao.id",idProfissao);
    }

    @RequestMapping(value = "/profissao/all", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8",
            method = {RequestMethod.GET, RequestMethod.POST})
    public
    List<ProfissaoEntity> listAllProfissao() {
        return this.ranchucrutesService.listAll(ProfissaoEntity.class);
    }

    @RequestMapping(value = "/cep/{cep}", produces = MediaType.APPLICATION_JSON_VALUE+ ";charset=UTF-8")
    public EnderecoVo getCep(@PathVariable String cep) {
        try {
            EnderecoEntity endereco = this.cepService.find(cep);
            return RanchucrutesAdapter.toEnderecoVo(endereco);
        } catch (CepNotFoundException e) {
            LOG.error("Cep nao encontrado", e);
        }
        return new EnderecoVo();
    }

}
