describe("First test", () => {

  it("Flex visit", () => {
    cy.visit(`https://flex-solution.com/`);
  });

 it("URL address is correct", () => {
      cy.location().should((loc) => {
        expect(loc.href).to.eq(`https://flex-solution.com/`)
      })
    });

  
});
