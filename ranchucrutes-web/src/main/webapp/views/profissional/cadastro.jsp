<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html lang="pt">
<head>
  <jsp:include page="/views/commons/header.jsp" />
  <jsp:include page="/views/commons/header-components.jsp" />
  <link rel="stylesheet" href="/static/css/libs/chosen.css">
  <link href="/static/css/menu.css" rel="stylesheet">
	<link href="/static/css/style.css" rel="stylesheet">
	<link href="/static/css/profissional/cadastro.css" rel="stylesheet">
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body class="cadastro-bg">
    <jsp:include page="/views/commons/menu_interna.jsp" />

    <div class="cadastro container content" >
        <section id="cadastro" style="margin-top:80px;">
            <form id="formCadastro" class="form-horizontal" action="/profissional/save" method="POST">
                <div class="col-md-7">


                    <div class="panel panel-default">
                      <div class="panel-body">
                        <h2>CADASTRE-SE</h2>
                        <c:if test="${errorMessage != null}">
                            <div class="alert alert-danger" role="alert">${errorMessage}</div>
                        </c:if>
                      </div>

                      <div class="panel-footer">

                            <div class="form-group">
                                <label class="col-md-12 control-label">Nome:<font style="color: rgb(169, 68, 66);">*</font> </label>
                                <div class="col-md-12">
                                    <input type="text" class="form-control" name="nome" placeholder="Nome Completo" maxlength="80" value="${form.nome}" required/>
                                </div>
                            </div>

						   <div class="row">
							<div class="col-md-6">
                            <div class="form-group">
                                <label class="col-md-12 control-label">Número Registro:<font style="color: rgb(169, 68, 66);">*</font></label>
                                <div class="col-md-12">
                                    <input type="number" class="form-control" name="numeroRegistro" placeholder="00000" maxlength="10" value="${form.numeroRegistro}" required/>
                                </div>
                            </div>
							</div>
							<div class="col-md-6">
                            <div class="form-group">
                                <label class="col-md-12 control-label">CPF:<font style="color: rgb(169, 68, 66);">*</font></label>
                                <div class="col-md-12">
                                    <input type="number" class="form-control" name="cpf" placeholder="CPF" maxlength="11" value="${form.cpf}" required/>
                                </div>
                            </div>
							</div>
						   </div>
						   

                            <div class="form-group">
                                <label class="col-md-12 control-label">Especialidade:<font style="color: rgb(169, 68, 66);">*</font></label>
                                <div class="col-md-12">
                                    <select id="idEspecialidade" name="idEspecialidade" data-placeholder="Selecione suas especialidades" multiple class="form-control chosen-select" required></select>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-md-12 control-label">Celular:</label>
                                <div class="col-md-3">
                                    <input type="number" class="form-control" name="ddd" placeholder="DDD" maxlength="2" value="${form.ddd}" />
                                </div>
                                <div class="col-md-9">
                                    <input type="number" class="form-control" name="celular" placeholder="Celular" maxlength="9" value="${form.celular}" />
                                </div>
                            </div>

                           <div class="form-group">
                               <label class="col-md-12 control-label">Email:<font style="color: rgb(169, 68, 66);">*</font></label>
                               <div class="col-md-12">
                                   <input type="text" class="form-control" name="email" placeholder="Email" maxlength="100" value="${form.email}" required/>
                               </div>
                           </div>

						   <div class="row">
							<div class="col-md-6">
                           <div class="form-group">
                              <label class="col-md-12 control-label">Senha:<font style="color: rgb(169, 68, 66);">*</font></label>
                              <div class="col-md-12">
                                  <input type="password" class="form-control" name="senha" placeholder="Senha" maxlength="10" required />
                              </div>
                          </div>
							</div>
							<div class="col-md-6">
                           <div class="form-group">
                              <label class="col-md-12 control-label">Confirmação:<font style="color: rgb(169, 68, 66);">*</font></label>
                              <div class="col-md-12">
                                  <input type="password" class="form-control" name="confirmacao" placeholder="Repita a Senha" maxlength="10" required/>
                              </div>
                          </div>
							</div>
						   </div>

                            <div class="form-group">
                                <div class="col-md-12 text-right">
                                    <button type="submit" class="btn btn-primary">Enviar</button>
                                </div>
                            </div>

                      </div>
                    </div>

                </div>
            </form>
        </section>



    <footer>
		<div id="rodape" class="col-sm-12 text-center">
			<img src="/static/img/logo_mini_white.png" />&nbsp;&nbsp;&nbsp; Seu portal de agendamento online</i>
		</div>
	</footer>
    </div>

    <jsp:include page="/views/commons/footer.jsp" />
    <jsp:include page="/views/commons/footer-components.jsp" />
    <script src="/static/js/ws/ranchucrutes-ws-client.js"></script>
    <script src="/static/js/commons/utils.js"></script>
    <script src="/static/js/profissional/cadastro.js"></script>
    <script src="/static/js/libs/cbpAnimatedHeader.js"></script>
<script>
$(document).ready(function() {
    var viewport = document.querySelector("meta[name=viewport]");
    if($(window).width() < 480){
        viewport.setAttribute('content', 'width=480, maximum-scale=1.0, user-scalable=no');
    }else{
        viewport.setAttribute('content', 'width=device-width, maximum-scale=1, user-scalable=no');
    }
});
</script>
    <script>
        var especSelected = [];
        <c:forEach var="esp" items="${form.idEspecialidade}">
            especSelected.push("${esp}");
        </c:forEach>

        $('li > a').filter(function(index){
            if (this.href.indexOf("cadastro") > 0 ) {
                $(this).parent().addClass('active');
            }
        });

    </script>

</body>
</html>