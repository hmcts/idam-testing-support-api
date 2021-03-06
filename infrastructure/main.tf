terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "=2.85.0"
    }
  }
}

provider "azurerm" {
  features {}
}

locals {
  default_name = "${var.product}-${var.component}"
  vault_name   = "${var.product}-${var.env}"
  instance_count = (var.env == "prod" || var.env == "idam-prod" || var.env == "idam-prod2") ? 0 : 1
  environment  = var.env == "idam-prod" ? "production" : var.env == "idam-aat" ? "staging" : var.env == "idam-perftest" ? "testing" : replace(var.env, "idam-", "")
  tags = merge(
    var.common_tags,
    {
      "environment"  = local.environment,
      "Team Name"    = "IdAM",
      "businessArea" = "CFT",
      "contactSlackChannel" = "idam-team",
      "managedBy"           = "idam",
      "application"         = "idam-testing-support-api"
    },
  )
}

module "idam-testing-support-api-db" {
  count              = local.instance_count
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = local.default_name
  location           = var.location
  env                = var.env
  subscription       = var.subscription
  postgresql_user    = "idamtstsptapi"
  database_name      = "idamtstsptapi"
  postgresql_version = 11
  sku_name           = "GP_Gen5_4"
  sku_tier           = "GeneralPurpose"
  sku_capacity       = "4"
  storage_mb         = "51200"
  common_tags        = local.tags
}

data "azurerm_key_vault" "default" {
  name                = local.vault_name
  resource_group_name = "${var.product}-${var.env}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-USER"
  value        = module.idam-testing-support-api-db[0].user_name
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PASS"
  value        = module.idam-testing-support-api-db[0].postgresql_password
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-HOST"
  value        = module.idam-testing-support-api-db[0].host_name
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PORT"
  value        = module.idam-testing-support-api-db[0].postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-DATABASE"
  value        = module.idam-testing-support-api-db[0].postgresql_database
  key_vault_id = data.azurerm_key_vault.default.id
}
