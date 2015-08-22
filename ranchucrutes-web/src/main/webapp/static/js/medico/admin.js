var admin = function() {
  return {
        init: function(){
            admin.initValidate();
            admin.initComboEspecialidade();
            admin.initComboConvenio();
            admin.initInput();
        },
        initInput: function(){
            $('[data-toggle="tooltip"]').tooltip();
            $('#valorConsulta').maskMoney({
               				prefix:'',
               				allowNegative: true,
               				thousands:'.',
               				decimal:',',
               				affixesStay: false
               			});
               			/*$('#valorConsulta').bind('change',function(){
               				$("#formCadastro").formValidation('updateStatus', 'pesoKg', 'NOT_VALIDATED')
            				.formValidation('validateField', 'pesoKg');

               			});*/
        },
        initComboEspecialidade: function(){
             RanchucrutesWS.listAllEspecialidade(function(especialidades){
                $('#idEspecialidade').html("<option/>");

                    $.each(especialidades, function(index) {
                        $('#idEspecialidade').append($("<option/>", {
                            value: especialidades[index].id,
                            text: especialidades[index].nome
                        }));
                    });



                    for (var i = 0; i < especSelected.length; i++){
                        $("#idEspecialidade option[value=" + especSelected[i] + "]").attr('selected','selected');
                    }



                    /*$('#idEspecialidade')
                    .chosen({no_results_text:'Oops, especialidade não encontrada!'})
                    .change( function(){
                       //nada
                    } );*/
                    $('#idEspecialidade').select2({placeholder:"Selecione uma especialidade"});

                });


        },
        initComboConvenio: function(){
             RanchucrutesWS.listAllConvenioCategorias(function(categorias){
                $('#idPlano').html("<option/>");

                    $.each(categorias, function(index) {
                        $('#idPlano').append($("<option/>", {
                            value: categorias[index].id,
                            text: categorias[index].nome
                        }));
                    });

                    /*
                    for (var i = 0; i < categoriaSelected.length; i++){
                        $("#idPlano option[value=" + categoriaSelected[i] + "]").attr('selected','selected');
                    }*/

                    $('#idPlano').select2({placeholder:"Selecione um plano de saúde"});

                });

        },
        initValidate: function(){
                 $('#formCadastro').formValidation({
                        excluded: ':disabled',
                        message: 'Valor inválido',
                        locale: 'pt_BR',
                        icon: {
                            valid: 'glyphicon glyphicon-ok',
                            invalid: 'glyphicon glyphicon-remove',
                            validating: 'glyphicon glyphicon-refresh'
                        },
                        fields: {
                            celular:{
                                validators: {
                                        regexp: {
                                            regexp: /^([0-9]+)$/,
                                            message: 'Número de celular inválido'
                                        }
                                }
                            },
                            ddd:{
                                validators: {
                                        regexp: {
                                            regexp: /^([0-9]+)$/,
                                            message: 'Número de DDD inválido'
                                        }
                                }
                            },
                            crm:{
                                validators: {
                                        regexp: {
                                            regexp: /^([0-9]+)$/,
                                            message: 'Número de CRM inválido'
                                        }
                                }
                            },
                            cpf: {
                                validators: {
                                    callback: {
                                        message: 'Cpf Inválido',
                                        callback: function(value, validator) {
                                            return Utils.validarCPF(value);
                                        }
                                    }
                                }
                            },
                            idEspecialidade: {
                                validators: {
                                    callback: {
                                        message: 'Selecione uma especialidade',
                                        callback: function(value, validator) {
                                            return value != '';
                                        }
                                    }
                                }
                            },
                            confirmacao: {
                                validators: {
                                    callback: {
                                        message: 'Confirmação da senha inválida.',
                                        callback: function(value, validator) {
                                            return $("input[name='senha']").val() == value;
                                        }
                                    }
                                }
                            }

                        }
                }).on('err.field.fv', function(e, data) {
                    if (data.fv.getSubmitButton()) {
                        data.fv.disableSubmitButtons(false);
                    }
                })
                .on('success.field.fv', function(e, data) {
                    if (data.fv.getSubmitButton()) {
                        data.fv.disableSubmitButtons(false);
                    }
                });
        }
  }
}();


$(document).ready(function(){
  admin.init();
});


