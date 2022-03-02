terraform {
  required_version = ">= 0.12"
}

terraform {
  backend "azurerm" {}
}

provider "azurerm" {
  version = "~> 2.17.0"
  features {
  }
}

locals {
  default_name = "${var.product}-${var.component}"
  vault_name   = "${var.product}-${var.env}"
  instance_count = var.env == "idam-preview" ? 1 : 0
  tags = merge(
    var.common_tags,
    {
      "environment" = var.env == "idam-preview" && var.product == "idam" ? "idam-dev" : var.env
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
  postgresql_user    = "idamtestingsupportapi"
  database_name      = "idamtestingsupportapi"
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
  value        = module.idam-api-db.user_name
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PASS"
  value        = module.idam-api-db.postgresql_password
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-HOST"
  value        = module.idam-api-db.host_name
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PORT"
  value        = module.idam-api-db.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-DATABASE"
  value        = module.idam-api-db.postgresql_database
  key_vault_id = data.azurerm_key_vault.default.id
}
